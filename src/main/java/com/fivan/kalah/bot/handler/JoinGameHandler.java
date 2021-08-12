package com.fivan.kalah.bot.handler;

import static com.fivan.kalah.bot.handler.InitialStateHandler.START_COMMAND;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.KeyboardService;
import com.fivan.kalah.bot.LobbySendMessageAction;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.service.GameService;
import com.fivan.kalah.service.LobbyService;
import com.fivan.kalah.util.GameUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinGameHandler {

  private final LobbyService lobbyService;
  private final GameService gameService;
  private final KeyboardService keyboardService;

  public Optional<HandlingResult> handle(Update update) {
    var botApiMethods = new ArrayList<BotApiMethod<?>>();
    var actions = new ArrayList<LobbySendMessageAction>();

    String[] splitMessage = update.getMessage().getText().split(" ");

    if (splitMessage.length != 2 || !splitMessage[0].equals(START_COMMAND)) {
      return Optional.empty();
    }

    String potentialLobbyId = splitMessage[1];
    try {
      UUID lobbyId = UUID.fromString(potentialLobbyId);
      Lobby lobby = lobbyService.findById(lobbyId)
          .orElseThrow(() -> new IllegalArgumentException("Couldn't find lobby with id " + lobbyId));
      Integer opponentPlayerId = lobby.getPlayerId();
      Integer playerId = GameUtils.getUserIdFromMessage(update);
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

    return Optional.of(HandlingResult.builder()
        .event(Event.TO_MENU)
        .methods(botApiMethods)
        .actions(actions)
        .build());
  }
}
