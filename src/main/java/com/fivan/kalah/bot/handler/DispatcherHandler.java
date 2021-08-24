package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.State;
import com.fivan.kalah.entity.PlayerState;
import com.fivan.kalah.repository.PlayerStateRepository;
import com.fivan.kalah.util.GameUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.romangr.simplestatemachine.StateMachine;
import ru.romangr.simplestatemachine.StateMachineConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DispatcherHandler {

  private final MakeMoveCallbackHandler moveCallbackHandler;
  private final StateMachineConfiguration<State, Event> stateEventStateMachine;
  private final PlayerStateRepository playerStateRepository;
  private final Map<State, StateHandler> handlersRoadMap;
  private final JoinGameHandler joinGameHandler;

  public DispatcherHandler(
      MakeMoveCallbackHandler moveCallbackHandler,
      PlayerStateRepository playerStateRepository,
      List<StateHandler> stateHandlers,
      JoinGameHandler joinGameHandler) {
    this.moveCallbackHandler = moveCallbackHandler;
    this.playerStateRepository = playerStateRepository;
    this.joinGameHandler = joinGameHandler;
    stateEventStateMachine =
        StateMachine.<State, Event>builder()
            .withStates(State.class)
            .withEvents(Event.class)
            .withInitialState(State.INITIAL)
            .withTransition(State.INITIAL, State.IN_MENU, Event.TO_MENU)
            .asStateMachineConfiguration();

    handlersRoadMap =
        stateHandlers.stream()
            .collect(
                Collectors.toMap(
                    stateHandler -> stateHandler.getClass().getAnnotation(Handler.class).value(),
                    Function.identity()));
  }

  public ActionsAndMethods handle(Update update) {
    Integer userId = GameUtils.getUserIdFromMessage(update);

    if (moveCallbackHandler.canProcess(update)) {
      return ActionsAndMethods.builder()
          .methods(moveCallbackHandler.handle(update, userId))
          .build();
    }

    Optional<PlayerState> playerStateOptional = playerStateRepository.findById(userId);

    // If player state is not present, we just call initial handler and add event to transitionsMap
    if (playerStateOptional.isEmpty()) {
      HandlingResult result = handlersRoadMap.get(State.INITIAL).handle(update);

      StateMachine<State, Event> playerStateMachine =
          StateMachine.fromInitialState(stateEventStateMachine);
      State currentState = playerStateMachine.acceptEvent(result.getEvent()).newState();

      playerStateRepository.save(new PlayerState(userId, currentState, null));

      return ActionsAndMethods.builder()
          .methods(addInitialMethodToCurrent(update, result.getMethods(), currentState))
          .actions(result.getActions())
          .build();
    }

    Optional<HandlingResult> joinGameResult = joinGameHandler.handle(update);
    if (joinGameResult.isPresent()) {
      HandlingResult handlingResult = joinGameResult.get();
      return ActionsAndMethods.builder()
          .actions(handlingResult.getActions())
          .methods(handlingResult.getMethods())
          .build();
    }

    State playerState = playerStateOptional.get().getState();
    HandlingResult result = handlersRoadMap.get(playerState).handle(update);

    StateMachine<State, Event> playerStateMachine =
        StateMachine.fromState(stateEventStateMachine, playerState);
    State newState = playerStateMachine.acceptEvent(result.getEvent()).newState();

    playerStateRepository.save(playerStateOptional.get().withState(newState));

    if (!playerState.equals(newState)) {
      return ActionsAndMethods.builder()
          .actions(result.getActions())
          .methods(addInitialMethodToCurrent(update, result.getMethods(), newState))
          .build();
    }

    return ActionsAndMethods.builder()
        .actions(result.getActions())
        .methods(result.getMethods())
        .build();
  }

  private ArrayList<BotApiMethod<?>> addInitialMethodToCurrent(
      Update update, List<BotApiMethod<?>> methods, State newState) {
    ArrayList<BotApiMethod<?>> botApiMethods = new ArrayList<>(methods);
    botApiMethods.addAll(handlersRoadMap.get(newState).getInitialMethods(update));
    return botApiMethods;
  }
}
