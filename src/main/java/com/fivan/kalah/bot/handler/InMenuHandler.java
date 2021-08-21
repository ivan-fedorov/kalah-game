package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.State;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.service.LobbyService;
import com.fivan.kalah.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import static com.fivan.kalah.util.GameUtils.getUserIdFromMessage;
import static java.util.Collections.singletonList;

@Handler(State.IN_MENU)
@RequiredArgsConstructor
public class InMenuHandler implements StateHandler {

  private static final String URL_TEMPLATE_START = "https://telegram.me/";
  private static final String URL_TEMPLATE_END = "?start=";
  private static final String CREATE_NEW_BUTTON_TEXT = "Create new";
  private static final String GAME_RULES_BUTTON_TEXT = "Show rules";
  private final LobbyService lobbyService;
  private final PlayerService playerService;
  private final ResourceBundle resourceBundle;

  @Value("${bot.telegram.name}")
  private String botName;

  @Override
  public HandlingResult handle(Update update) {
    var botApiMethods = new ArrayList<BotApiMethod<?>>();
    Integer userId = getUserIdFromMessage(update);
    if (update.getMessage().getText().equals(CREATE_NEW_BUTTON_TEXT)) {
      Lobby lobby =
          lobbyService.createLobby(
              Lobby.builder()
                  .id(UUID.randomUUID())
                  .playerId(userId)
                  .playerOne(playerService.getById(userId).orElseThrow())
                  .build());
      UUID lobbyId = lobby.getId();

      botApiMethods.add(
          new SendMessage()
              .setText(resourceBundle.getString("lobbyWasCreated"))
              .setChatId(userId.longValue())
              .setReplyMarkup(createLobbyKeyboard(lobbyId)));
    }
    if (update.getMessage().getText().equals(GAME_RULES_BUTTON_TEXT)) {
      botApiMethods.add(
          new SendMessage()
              .setParseMode(ParseMode.MARKDOWN)
              .setText(resourceBundle.getString("gameRules"))
              .setChatId(userId.longValue()));
    }

    return HandlingResult.builder().event(Event.TO_MENU).methods(botApiMethods).build();
  }

  @Override
  public List<BotApiMethod<?>> getInitialMethods(Update update) {
    Integer userId = getUserIdFromMessage(update);
    KeyboardRow gameRow = new KeyboardRow();

    gameRow.add(CREATE_NEW_BUTTON_TEXT);
    gameRow.add(GAME_RULES_BUTTON_TEXT);

    return singletonList(
        new SendMessage()
            .setText(resourceBundle.getString("chooseAction"))
            .setReplyMarkup(new ReplyKeyboardMarkup().setKeyboard(List.of(gameRow)))
            .setChatId(userId.longValue()));
  }

  private ReplyKeyboard createLobbyKeyboard(UUID lobbyId) {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
    inlineKeyboardButton.setText(resourceBundle.getString("joinGame"));
    inlineKeyboardButton.setUrl(URL_TEMPLATE_START + botName + URL_TEMPLATE_END + lobbyId);
    inlineKeyboardMarkup.setKeyboard(List.of(List.of(inlineKeyboardButton)));
    return inlineKeyboardMarkup;
  }
}
