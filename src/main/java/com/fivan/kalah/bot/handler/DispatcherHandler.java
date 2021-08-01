package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.State;
import com.fivan.kalah.util.GameUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.romangr.simplestatemachine.StateMachine;
import ru.romangr.simplestatemachine.StateMachineConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DispatcherHandler {

  private final MakeMoveCallbackHandler moveCallbackHandler;
  private final StateMachineConfiguration<State, Event> stateEventStateMachine;
  private final Map<Integer, State> playerStateStorage = new HashMap<>();
  private final Map<State, StateHandler> handlersRoadMap;

  public DispatcherHandler(MakeMoveCallbackHandler moveCallbackHandler, List<StateHandler> stateHandlers) {
    this.moveCallbackHandler = moveCallbackHandler;
    stateEventStateMachine = StateMachine.<State, Event>builder()
        .withStates(State.class)
        .withEvents(Event.class)
        .withInitialState(State.INITIAL)
        .withTransition(State.INITIAL, State.IN_MENU, Event.TO_MENU)
        .asStateMachineConfiguration();

    handlersRoadMap = stateHandlers.stream()
        .collect(Collectors.toMap(stateHandler ->
                stateHandler.getClass().getAnnotation(Handler.class).value(),
            Function.identity()
        ));
  }

  public ActionsAndMethods handle(Update update) {
    Integer userId = GameUtils.getUserIdFromMessage(update);

    if (moveCallbackHandler.canProcess(update)) {
      return ActionsAndMethods.builder()
          .methods(moveCallbackHandler.handle(update, userId))
          .build();
    }

    State playerState = playerStateStorage.get(userId);

    //If player state is not present, we just call initial handler and add event to transitionsMap
    if (playerState == null) {
      HandlingResult result = handlersRoadMap.get(State.INITIAL).handle(update);

      StateMachine<State, Event> playerStateMachine = StateMachine.fromInitialState(stateEventStateMachine);
      State currentState = playerStateMachine.acceptEvent(result.getEvent()).newState();

      playerStateStorage.put(userId, currentState);

      return ActionsAndMethods.builder()
          .methods(addInitialMethodToCurrent(update, result.getMethods(), currentState))
          .actions(result.getActions())
          .build();
    }

    HandlingResult result = handlersRoadMap.get(playerState).handle(update);

    StateMachine<State, Event> playerStateMachine = StateMachine.fromState(stateEventStateMachine, playerState);
    State newState = playerStateMachine.acceptEvent(result.getEvent()).newState();

    playerStateStorage.put(userId, newState);

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

  private ArrayList<BotApiMethod<?>> addInitialMethodToCurrent(Update update, List<BotApiMethod<?>> methods, State newState) {
    ArrayList<BotApiMethod<?>> botApiMethods = new ArrayList<>(methods);
    botApiMethods.addAll(handlersRoadMap.get(newState).getInitialMethods(update));
    return botApiMethods;
  }
}
