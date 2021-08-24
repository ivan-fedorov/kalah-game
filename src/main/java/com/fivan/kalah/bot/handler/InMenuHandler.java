package com.fivan.kalah.bot.handler;

import static com.fivan.kalah.util.GameUtils.getUserIdFromMessage;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.State;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.service.LobbyService;
import com.fivan.kalah.service.PlayerService;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
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

@Handler(State.IN_MENU)
public class InMenuHandler implements StateHandler {

  private static final String URL_TEMPLATE_START = "https://telegram.me/";
  private static final String URL_TEMPLATE_END = "?start=";
  private final LobbyService lobbyService;
  private final PlayerService playerService;
  private final ResourceBundle messageBundle;
  private final String botName;
  private final String readRulesButtonText;
  private final String createNewButtonText;
  private final String showTopTenPlayerButtonText;
  private final String positionColumnName;
  private final String nameColumnName;
  private final String ratingColumnName;
  private final String unknownCommandText;

  public InMenuHandler(
      LobbyService lobbyService,
      PlayerService playerService,
      @Value("${bot.telegram.name}") String botName,
      ResourceBundle messageBundle) {
    this.lobbyService = lobbyService;
    this.playerService = playerService;
    this.messageBundle = messageBundle;
    this.botName = botName;
    this.createNewButtonText = messageBundle.getString("createNewButtonText");
    this.readRulesButtonText = messageBundle.getString("readRulesButtonText");
    this.showTopTenPlayerButtonText = messageBundle.getString("showTopTenPlayerButtonText");
    this.positionColumnName = messageBundle.getString("positionColumnName");
    this.nameColumnName = messageBundle.getString("nameColumnName");
    this.ratingColumnName = messageBundle.getString("ratingColumnName");
    this.unknownCommandText = messageBundle.getString("unknownMenuCommand");
  }

  @Override
  public HandlingResult handle(Update update) {
    var botApiMethods = new ArrayList<BotApiMethod<?>>();
    Integer userId = getUserIdFromMessage(update);
    if (update.getMessage().getText().equals(createNewButtonText)) {
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
              .setText(messageBundle.getString("lobbyWasCreated"))
              .setChatId(userId.longValue())
              .setReplyMarkup(createLobbyKeyboard(lobbyId)));
    }
    if (update.getMessage().getText().equals(readRulesButtonText)) {
      botApiMethods.add(
          new SendMessage()
              .setParseMode(ParseMode.MARKDOWN)
              .setText(messageBundle.getString("gameRules"))
              .setReplyMarkup(buildMenuKeyboard())
              .setChatId(userId.longValue()));
    }
    if (update.getMessage().getText().equals(showTopTenPlayerButtonText)) {
      List<Player> topTenPlayers = playerService.findTopTenPlayers();

      int maxNameSize =
          topTenPlayers.stream().map(Player::getName).mapToInt(String::length).max().orElseThrow();
      ArrayList<List<String>> tableRow = new ArrayList<>();
      tableRow.add(List.of(positionColumnName, nameColumnName, ratingColumnName));
      tableRow.add(
          List.of(
              "-".repeat(positionColumnName.length()),
              "-".repeat(maxNameSize),
              "-".repeat(ratingColumnName.length())));
      for (int i = 0; i < topTenPlayers.size(); i++) {
        tableRow.add(
            List.of(
                String.valueOf(i + 1),
                topTenPlayers.get(i).getName(),
                topTenPlayers.get(i).getRating().toString()));
      }

      String tableText =
          tableRow.stream()
              .map(
                  row ->
                      List.of(
                          Strings.padEnd(row.get(0), positionColumnName.length(), ' '),
                          Strings.padEnd(row.get(1), maxNameSize, ' '),
                          Strings.padEnd(row.get(2), ratingColumnName.length(), ' ')))
              .map(row -> row.stream().collect(joining(" | ", "| ", " |")))
              .collect(joining("\n", "```\n", "\n```"));

      botApiMethods.add(
          new SendMessage()
              .setParseMode(ParseMode.MARKDOWN)
              .setText(tableText)
              .setReplyMarkup(buildMenuKeyboard())
              .setChatId(userId.longValue()));
    }

    if (botApiMethods.isEmpty()) {
      botApiMethods.add(
          new SendMessage()
              .setText(this.unknownCommandText)
              .setReplyMarkup(buildMenuKeyboard())
              .setChatId(userId.longValue()));
    }

    return HandlingResult.builder().event(Event.TO_MENU).methods(botApiMethods).build();
  }

  @Override
  public List<BotApiMethod<?>> getInitialMethods(Update update) {
    Integer userId = getUserIdFromMessage(update);

    return singletonList(
        new SendMessage()
            .setText(messageBundle.getString("chooseAction"))
            .setReplyMarkup(buildMenuKeyboard())
            .setChatId(userId.longValue()));
  }

  private ReplyKeyboardMarkup buildMenuKeyboard() {
    KeyboardRow gameRow = new KeyboardRow();
    gameRow.add(createNewButtonText);
    gameRow.add(readRulesButtonText);
    gameRow.add(showTopTenPlayerButtonText);
    return new ReplyKeyboardMarkup().setKeyboard(List.of(gameRow));
  }

  private ReplyKeyboard createLobbyKeyboard(UUID lobbyId) {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
    inlineKeyboardButton.setText(messageBundle.getString("joinGame"));
    inlineKeyboardButton.setUrl(URL_TEMPLATE_START + botName + URL_TEMPLATE_END + lobbyId);
    inlineKeyboardMarkup.setKeyboard(List.of(List.of(inlineKeyboardButton)));
    return inlineKeyboardMarkup;
  }
}
