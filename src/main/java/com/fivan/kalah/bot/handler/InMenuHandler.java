package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.State;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.service.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fivan.kalah.util.GameUtils.getUserIdFromMessage;
import static java.util.Collections.singletonList;

@Handler(State.IN_MENU)
@RequiredArgsConstructor
public class InMenuHandler implements StateHandler {

  @Value("${bot.telegram.name}")
  private String botName;

  private static final String URL_TEMPLATE_START = "https://telegram.me/";
  private static final String URL_TEMPLATE_END = "?start=";
  private static final String CREATE_NEW = "Create new";
  private final LobbyService lobbyService;

  @Override
  public HandlingResult handle(Update update) {
    var botApiMethods = new ArrayList<BotApiMethod<?>>();
    Integer userId = getUserIdFromMessage(update);
    if (update.getMessage().getText().equals(CREATE_NEW)) {
      Lobby lobby = lobbyService.createLobby(Lobby.builder()
          .id(UUID.randomUUID())
          .playerId(userId)
          .build());
      UUID lobbyId = lobby.getId();

      botApiMethods.add(new SendMessage()
          .setText("Lobby was created, please forward this link to other player")
          .setChatId(userId.longValue())
          .setReplyMarkup(createLobbyKeyboard(lobbyId))
      );
    }

    return HandlingResult.builder()
        .event(Event.TO_MENU)
        .methods(botApiMethods)
        .build();
  }

  @Override
  public List<BotApiMethod<?>> getInitialMethods(Update update) {
    Integer userId = getUserIdFromMessage(update);
    KeyboardRow gameRow = new KeyboardRow();

    gameRow.add(CREATE_NEW);

    return singletonList(new SendMessage()
        .setText("Choose your next action:")
        .setReplyMarkup(new ReplyKeyboardMarkup().setKeyboard(List.of(gameRow)))
        .setChatId(userId.longValue()));
  }

  private ReplyKeyboard createLobbyKeyboard(UUID lobbyId) {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
    inlineKeyboardButton.setText("Join game");
    inlineKeyboardButton.setUrl(
        URL_TEMPLATE_START
            + botName
            + URL_TEMPLATE_END
            + lobbyId
    );
    inlineKeyboardMarkup.setKeyboard(List.of(List.of(inlineKeyboardButton)));
    return inlineKeyboardMarkup;
  }

}
