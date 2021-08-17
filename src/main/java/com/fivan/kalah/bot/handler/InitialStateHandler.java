package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.State;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.service.PlayerService;
import com.fivan.kalah.util.GameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
@Handler(State.INITIAL)
@RequiredArgsConstructor
public class InitialStateHandler implements StateHandler {

  public static final String START_COMMAND = "/start";

  private final PlayerService playerService;
  private final JoinGameHandler joinGameHandler;
  private final ResourceBundle resourceBundle;

  @Override
  public HandlingResult handle(Update update) {
    var botApiMethods = new ArrayList<BotApiMethod<?>>();

    Integer playerId = GameUtils.getUserIdFromMessage(update);
    if (playerService.getById(playerId).isEmpty()) {
      String userName = GameUtils.getUsername(update.getMessage().getFrom());
      Player newPlayer = new Player(playerId, userName, 1000, null);
      playerService.save(newPlayer);
      botApiMethods.add(
          new SendMessage()
              .setText(String.format(resourceBundle.getString("welcomeMessage"), userName))
              .setChatId(playerId.longValue()));
    }

    Optional<HandlingResult> joinGameResults = joinGameHandler.handle(update);
    joinGameResults.ifPresent(results -> botApiMethods.addAll(results.getMethods()));

    return HandlingResult.builder()
        .event(Event.TO_MENU)
        .methods(botApiMethods)
        .actions(joinGameResults.map(HandlingResult::getActions).orElse(List.of()))
        .build();
  }
}
