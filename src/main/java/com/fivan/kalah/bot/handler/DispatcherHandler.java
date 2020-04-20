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

  private final StateMachineConfiguration<State, Event> stateEventStateMachine;
  private Map<Integer, State> playerStateStorage = new HashMap<>();
  private Map<State, StateHandler> handlersRoadMap;

  public DispatcherHandler(List<StateHandler> stateHandlers) {
    stateEventStateMachine = StateMachine.<State, Event>builder()
        .withStates(State.class)
        .withEvents(Event.class)
        .withInitialState(State.INITIAL)
        .withTransition(State.INITIAL, State.IN_MENU, Event.TO_MENU)
        .withTransition(State.IN_MENU, State.IN_GAME, Event.TO_GAME)
        .withTransition(State.IN_GAME, State.IN_MENU, Event.END_GAME)
        .asStateMachineConfiguration();

    handlersRoadMap = stateHandlers.stream()
        .collect(Collectors.toMap(stateHandler ->
                stateHandler.getClass().getAnnotation(Handler.class).value(),
            Function.identity()
        ));
  }

  public List<BotApiMethod<?>> handle(Update update) {
    Integer userId = GameUtils.getUserIdFromMessage(update);
    State playerState = playerStateStorage.get(userId);

    //If player state is not present, we just call initial handler and add event to transitionsMap
    if (playerState == null) {
      HandlingResult result = handlersRoadMap.get(State.INITIAL).handle(update);

      StateMachine<State, Event> playerStateMachine = StateMachine.fromInitialState(stateEventStateMachine);
      State currentState = playerStateMachine.acceptEvent(result.getEvent()).newState();

      playerStateStorage.put(userId, currentState);

      return addInitialMethodToCurrent(update, result.getMethods(), currentState);
    }

    HandlingResult result = handlersRoadMap.get(playerState).handle(update);

    StateMachine<State, Event> playerStateMachine = StateMachine.fromState(stateEventStateMachine, playerState);
    State newState = playerStateMachine.acceptEvent(result.getEvent()).newState();

    playerStateStorage.put(userId, newState);

    if (!playerState.equals(newState)) {
      return addInitialMethodToCurrent(update, result.getMethods(), newState);
    }

    return result.getMethods();

  }

  private ArrayList<BotApiMethod<?>> addInitialMethodToCurrent(Update update, List<BotApiMethod<?>> methods, State newState) {
    ArrayList<BotApiMethod<?>> botApiMethods = new ArrayList<>(methods);
    botApiMethods.addAll(handlersRoadMap.get(newState).getInitialMethods(update));
    return botApiMethods;
  }
}
