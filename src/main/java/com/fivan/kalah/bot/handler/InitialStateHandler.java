package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.State;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.service.PlayerService;
import com.fivan.kalah.util.GameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

@Component
@Handler(State.INITIAL)
@RequiredArgsConstructor
public class InitialStateHandler implements StateHandler {

  private static final String START_MESSAGE = "/start";
  private final PlayerService playerService;

  @Override
  public HandlingResult handle(Update update) {

    var botApiMethods = new ArrayList<BotApiMethod>();

    Integer userId = GameUtils.getUserIdFromMessage(update);
    if (playerService.getById(userId).isEmpty()) {
      String userName = update.getMessage().getFrom().getUserName();
      Player newPlayer = new Player(userId, userName);
      playerService.save(newPlayer);
      botApiMethods.add(new SendMessage()
          .setText(String.format("Hello %s, welcome to Kalah game!", userName))
          .setChatId(userId.longValue()));
    }

    return HandlingResult.builder()
        .event(Event.TO_MENU)
        .methods(botApiMethods)
        .build();
  }

}
