package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.KeyboardService;
import com.fivan.kalah.bot.handler.callback.CallbackDataFactory;
import com.fivan.kalah.bot.handler.callback.CallbackType;
import com.fivan.kalah.bot.handler.callback.MakeMoveCallbackData;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.GameStatus;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.exception.MakeMoveInFinishedGameException;
import com.fivan.kalah.service.GameService;
import com.fivan.kalah.service.GameService.MakeMoveResult;
import com.fivan.kalah.service.LobbyService;
import com.fivan.kalah.service.RatingCalculator.RatingCalculation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.ResourceBundle;

import static com.fivan.kalah.entity.GameStatus.InProgress;

@Component
@RequiredArgsConstructor
public class MakeMoveCallbackHandler {
  private final GameService gameService;
  private final CallbackDataFactory callbackDataFactory;
  private final LobbyService lobbyService;
  private final KeyboardService keyboardService;
  private final ResourceBundle resourceBundle;

  public boolean canProcess(Update update) {
    return update.getCallbackQuery() != null
        && update.getCallbackQuery().getData().startsWith(CallbackType.MAKE_MOVE.getPrefix());
  }

  public List<BotApiMethod<?>> handle(Update update, Integer playerId) {
    MakeMoveCallbackData callbackData =
        callbackDataFactory.toMakeMoveCallbackData(update.getCallbackQuery().getData());
    try {
      MakeMoveResult moveResult =
          gameService.makeMove(callbackData.getBoardId(), playerId, callbackData.getPitId());
      BoardRepresentation board = moveResult.getBoard();

      boolean isPlayerTwoTurn = board.getCurrentPlayer().equals(board.getPlayerTwo());

      Lobby lobby = lobbyService.getByBoardId(board.getId());
      EditMessageText sendPlayerOneMessage;
      EditMessageText sendPlayerTwoMessage;

      if (board.getGameStatus() != InProgress) {
        sendPlayerOneMessage =
            new EditMessageText()
                .setParseMode(ParseMode.MARKDOWN)
                .setText(finalMessageForPlayerOne(moveResult, lobby.getPlayerTwo())[0])
                .setChatId(board.getPlayerOne().longValue())
                .setReplyMarkup(keyboardService.preparePlayerOneButtons(board))
                .setMessageId(lobby.getPlayerOneMessageId());

        sendPlayerTwoMessage =
            new EditMessageText()
                .setParseMode(ParseMode.MARKDOWN)
                .setText(finalMessageForPlayerOne(moveResult, lobby.getPlayerOne())[1])
                .setChatId(board.getPlayerTwo().longValue())
                .setReplyMarkup(keyboardService.preparePlayerTwoButtons(board))
                .setMessageId(lobby.getPlayerTwoMessageId());
      } else {
        sendPlayerOneMessage =
            new EditMessageText()
                .setParseMode(ParseMode.MARKDOWN)
                .setText(
                    isPlayerTwoTurn
                        ? String.format(
                            resourceBundle.getString("opponentsTurn"),
                            lobby.getPlayerTwo().getName(),
                            lobby.getPlayerTwo().getId())
                        : String.format(
                            resourceBundle.getString("yourTurn"),
                            lobby.getPlayerOne().getName(),
                            lobby.getPlayerId()))
                .setChatId(board.getPlayerOne().longValue())
                .setReplyMarkup(keyboardService.preparePlayerOneButtons(board))
                .setMessageId(lobby.getPlayerOneMessageId());

        sendPlayerTwoMessage =
            new EditMessageText()
                .setParseMode(ParseMode.MARKDOWN)
                .setText(
                    isPlayerTwoTurn
                        ? String.format(
                            resourceBundle.getString("yourTurn"),
                            lobby.getPlayerOne().getName(),
                            lobby.getPlayerId())
                        : String.format(
                            resourceBundle.getString("opponentsTurn"),
                            lobby.getPlayerTwo().getName(),
                            lobby.getPlayerTwo().getId()))
                .setChatId(board.getPlayerTwo().longValue())
                .setReplyMarkup(keyboardService.preparePlayerTwoButtons(board))
                .setMessageId(lobby.getPlayerTwoMessageId());
      }
      return List.of(sendPlayerOneMessage, sendPlayerTwoMessage);
    } catch (MakeMoveInFinishedGameException e) {
      return List.of(
          new AnswerCallbackQuery()
              .setText(resourceBundle.getString("gameEnded"))
              .setCallbackQueryId(update.getCallbackQuery().getId()));
    }
  }

  private String[] finalMessageForPlayerOne(MakeMoveResult result, Player opponent) {
    GameStatus gameStatus = result.getBoard().getGameStatus();
    RatingCalculation rating = result.getRatingCalculation();
    switch (gameStatus) {
      case PlayerOneWins:
        return new String[] {
          String.format(
              resourceBundle.getString("playerWinMessage"),
              opponent.getName(),
              opponent.getId(),
              rating.getPlayerOneRating(),
              rating.getPlayerOneDelta()),
          String.format(
              resourceBundle.getString("playerLoseMessage"),
              opponent.getName(),
              opponent.getId(),
              rating.getPlayerTwoRating(),
              rating.getPlayerTwoDelta())
        };
      case PlayerTwoWins:
        return new String[] {
          String.format(
              resourceBundle.getString("playerLoseMessage"),
              opponent.getName(),
              opponent.getId(),
              rating.getPlayerOneRating(),
              rating.getPlayerOneDelta()),
          String.format(
              resourceBundle.getString("playerWinMessage"),
              opponent.getName(),
              opponent.getId(),
              rating.getPlayerTwoRating(),
              rating.getPlayerTwoDelta())
        };
      case Draw:
        return new String[] {
          String.format(
              resourceBundle.getString("drawMessage"),
              opponent.getName(),
              opponent.getId(),
              rating.getPlayerOneRating(),
              rating.getPlayerOneDelta()),
          String.format(
              resourceBundle.getString("drawMessage"),
              opponent.getName(),
              opponent.getId(),
              rating.getPlayerTwoRating(),
              rating.getPlayerTwoDelta())
        };
    }
    throw new IllegalStateException(String.format("Unhandled board state: %s", gameStatus));
  }
}
