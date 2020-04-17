package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.State;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.service.GameService;
import com.fivan.kalah.service.LobbyService;
import com.fivan.kalah.service.PlayerService;
import com.fivan.kalah.util.GameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Handler(State.INITIAL)
@RequiredArgsConstructor
public class InitialStateHandler implements StateHandler {

  public static final String START_COMMAND = "/start";

  private final PlayerService playerService;
  private final LobbyService lobbyService;
  private final GameService gameService;

  @Override
  public HandlingResult handle(Update update) {

    var botApiMethods = new ArrayList<BotApiMethod>();

    Integer playerId = GameUtils.getUserIdFromMessage(update);
    if (playerService.getById(playerId).isEmpty()) {
      String userName = update.getMessage().getFrom().getUserName();
      Player newPlayer = new Player(playerId, userName);
      playerService.save(newPlayer);
      botApiMethods.add(new SendMessage()
          .setText(String.format("Hello %s, welcome to Kalah game!", userName))
          .setChatId(playerId.longValue()));
    }

    String[] splitMessage = update.getMessage().getText().split(" ");

    if (splitMessage[0].equals(START_COMMAND) && splitMessage.length == 2) {
      String potentialLobbyId = splitMessage[1];
      try {
        UUID lobbyId = UUID.fromString(potentialLobbyId);
        Lobby lobby = lobbyService.getById(lobbyId).orElseThrow();
        Integer opponentPlayerId = lobby.getPlayerId();
        BoardRepresentation board = gameService.createGame(opponentPlayerId, playerId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> currentPlayerRow = fillCurrentPlayerRow(playerId, board);
        List<InlineKeyboardButton> opponentPlayerRow = fillOpponentPlayerRow(lobby.getPlayerId(), board);

        inlineKeyboardMarkup.setKeyboard(List.of(opponentPlayerRow, currentPlayerRow));

        SendMessage sendMessage = new SendMessage()
            .setText("Opponent move")
            .setChatId(playerId.longValue())
            .setReplyMarkup(inlineKeyboardMarkup);

        botApiMethods.add(sendMessage);

      } catch (IllegalArgumentException e) {
        log.warn("{} is not correct UUID", potentialLobbyId, e);
      }
    }

    return HandlingResult.builder()
                         .event(Event.TO_MENU)
                         .methods(botApiMethods)
                         .build();
  }

  private List<InlineKeyboardButton> fillCurrentPlayerRow(Integer playerId, BoardRepresentation board) {
    LinkedList<InlineKeyboardButton> currentPlayerRow = new LinkedList<>();

    List<Integer> playerPits = board.getPlayerPits(playerId);

    int pitSize = playerPits.size() - 1;
    for (int i = 0; i <= pitSize; i++) {
      Integer pit = playerPits.get(i);
      InlineKeyboardButton pitButton = new InlineKeyboardButton()
          .setText(pit.toString())
          .setCallbackData("makeMove:" + board.getId() + ":" + (pitSize - i));

      currentPlayerRow.addFirst(pitButton);
    }

    currentPlayerRow.addFirst(new InlineKeyboardButton().setText("-").setCallbackData("random"));
    return currentPlayerRow;
  }

  private List<InlineKeyboardButton> fillOpponentPlayerRow(Integer playerId, BoardRepresentation board) {
    LinkedList<InlineKeyboardButton> currentPlayerRow = new LinkedList<>();

    List<Integer> playerPits = board.getPlayerPits(playerId);

    for (Integer pit : playerPits) {
      InlineKeyboardButton pitButton = new InlineKeyboardButton()
          .setText(pit.toString())
          .setCallbackData("no-op");

      currentPlayerRow.addFirst(pitButton);
    }

    currentPlayerRow.add(new InlineKeyboardButton().setText("-").setCallbackData("no-op"));

    return currentPlayerRow;
  }

}
