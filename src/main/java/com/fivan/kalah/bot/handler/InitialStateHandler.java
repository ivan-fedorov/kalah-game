package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.*;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.service.GameService;
import com.fivan.kalah.service.LobbyService;
import com.fivan.kalah.service.PlayerService;
import com.fivan.kalah.util.GameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Handler(State.INITIAL)
@RequiredArgsConstructor
public class InitialStateHandler implements StateHandler {

  public static final String START_COMMAND = "/start";

  private final PlayerService playerService;
  private final LobbyService lobbyService;
  private final GameService gameService;
  private final KeyboardService keyboardService;

  @Override
  public HandlingResult handle(Update update) {

    var botApiMethods = new ArrayList<BotApiMethod<?>>();
    var actions = new ArrayList<LobbySendMessageAction>();

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
        Lobby lobby = lobbyService.findById(lobbyId).orElseThrow();
        Integer opponentPlayerId = lobby.getPlayerId();
        BoardRepresentation board = gameService.createGame(opponentPlayerId, playerId);
        lobbyService.addBoardId(lobbyId, board.getId());

        SendMessage sendPlayerOneMessage = new SendMessage()
            .setText("Your turn")
            .setChatId(board.getPlayerOne().longValue())
            .setReplyMarkup(keyboardService.preparePlayerOneButtons(board));

        SendMessage sendPlayerTwoMessage =
            new SendMessage()
                .setText("Opponents turn")
                .setChatId(board.getPlayerTwo().longValue())
                .setReplyMarkup(keyboardService.preparePlayerTwoButtons(board));

        LobbySendMessageAction playerOneAction = new LobbySendMessageAction(sendPlayerOneMessage, message ->
            lobbyService.addPlayerOneMessageId(lobbyId, message.getMessageId())
        );
        LobbySendMessageAction playerTwoAction = new LobbySendMessageAction(sendPlayerTwoMessage, message ->
            lobbyService.addPlayerTwoMessageId(lobbyId, message.getMessageId())
        );
        actions.addAll(List.of(playerOneAction, playerTwoAction));
      } catch (IllegalArgumentException e) {
        log.warn("{} is not correct UUID", potentialLobbyId, e);
      }
    }

    return HandlingResult.builder()
        .event(Event.TO_MENU)
        .methods(botApiMethods)
        .actions(actions)
        .build();
  }
}
