package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.Event;
import com.fivan.kalah.bot.HandlingResult;
import com.fivan.kalah.bot.KeyboardService;
import com.fivan.kalah.bot.LobbySendMessageAction;
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
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

import static com.fivan.kalah.bot.handler.InitialStateHandler.START_COMMAND;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinGameHandler {

  private final LobbyService lobbyService;
  private final GameService gameService;
  private final PlayerService playerService;
  private final ResourceBundle messageBundle;
  private final KeyboardService keyboardService;

  public Optional<HandlingResult> handle(Update update) {
    var botApiMethods = new ArrayList<BotApiMethod<?>>();
    var actions = new ArrayList<LobbySendMessageAction>();

    String[] splitMessage = update.getMessage().getText().split(" ");

    if (splitMessage.length != 2 || !splitMessage[0].equals(START_COMMAND)) {
      return Optional.empty();
    }

    Integer playerId = GameUtils.getUserIdFromMessage(update);

    String potentialLobbyId = splitMessage[1];
    try {
      UUID lobbyId = UUID.fromString(potentialLobbyId);
      Optional<Lobby> optionalLobby = lobbyService.findEmptyLobbyById(lobbyId);
      if (optionalLobby.isEmpty()) {
        SendMessage joinFailedMessage =
            new SendMessage()
                .setText(messageBundle.getString("lobbyIsFull"))
                .setChatId(playerId.toString());

        botApiMethods.add(joinFailedMessage);

        return Optional.of(
            HandlingResult.builder()
                .event(Event.TO_MENU)
                .methods(botApiMethods)
                .actions(actions)
                .build());
      }

      Lobby lobby = optionalLobby.get();
      Integer opponentPlayerId = lobby.getPlayerId();
      BoardRepresentation board = gameService.createGame(opponentPlayerId, playerId);
      Player playerTwo = playerService.getById(playerId).orElseThrow();
      lobbyService.handlePlayerTwoJoining(lobbyId, board.getId(), playerTwo);

      SendMessage sendPlayerOneMessage =
          new SendMessage()
              .setParseMode(ParseMode.MARKDOWN)
              .setText(
                  String.format(
                      messageBundle.getString("yourTurn"), playerTwo.getName(), playerTwo.getId()))
              .setChatId(board.getPlayerOne().longValue())
              .setReplyMarkup(keyboardService.preparePlayerOneButtons(board));

      SendMessage sendPlayerTwoMessage =
          new SendMessage()
              .setParseMode(ParseMode.MARKDOWN)
              .setText(
                  String.format(
                      messageBundle.getString("opponentsTurn"),
                      lobby.getPlayerOne().getName(),
                      lobby.getPlayerOne().getId()))
              .setChatId(board.getPlayerTwo().longValue())
              .setReplyMarkup(keyboardService.preparePlayerTwoButtons(board));

      LobbySendMessageAction playerOneAction =
          new LobbySendMessageAction(
              sendPlayerOneMessage,
              message -> lobbyService.addPlayerOneMessageId(lobbyId, message.getMessageId()));
      LobbySendMessageAction playerTwoAction =
          new LobbySendMessageAction(
              sendPlayerTwoMessage,
              message -> lobbyService.addPlayerTwoMessageId(lobbyId, message.getMessageId()));
      actions.addAll(List.of(playerOneAction, playerTwoAction));
    } catch (IllegalArgumentException e) {
      log.warn("{} is not correct UUID", potentialLobbyId, e);
    }

    return Optional.of(
        HandlingResult.builder()
            .event(Event.TO_MENU)
            .methods(botApiMethods)
            .actions(actions)
            .build());
  }
}
