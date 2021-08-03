package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.KeyboardService;
import com.fivan.kalah.bot.handler.callback.CallbackDataFactory;
import com.fivan.kalah.bot.handler.callback.CallbackType;
import com.fivan.kalah.bot.handler.callback.MakeMoveCallbackData;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.GameStatus;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.exception.MakeMoveInFinishedGameException;
import com.fivan.kalah.service.GameService;
import com.fivan.kalah.service.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.fivan.kalah.entity.GameStatus.InProgress;

@Component
@RequiredArgsConstructor
public class MakeMoveCallbackHandler {
  private static final String YOUR_TURN_MESSAGE = "Your turn ⬇️";
  private static final String OPPONENTS_TURN_️MESSAGE = "Opponents turn ⬆️";
  private static final String PLAYER_WIN_MESSAGE = "Chicken dinner, you are winner \uD83D\uDC51";
  private static final String PLAYER_LOSE_MESSAGE = "You lose! Good day sir \uD83D\uDE14";
  private static final String DRAW_MESSAGE = "DRAW \uD83E\uDD1D";
  private final GameService gameService;
  private final CallbackDataFactory callbackDataFactory;
  private final LobbyService lobbyService;
  private final KeyboardService keyboardService;

  public boolean canProcess(Update update) {
    return update.getCallbackQuery() != null && update.getCallbackQuery().getData().startsWith(CallbackType.MAKE_MOVE.getPrefix());
  }

  public List<BotApiMethod<?>> handle(Update update, Integer playerId) {
    MakeMoveCallbackData callbackData = callbackDataFactory.toMakeMoveCallbackData(update.getCallbackQuery().getData());
    try {
      BoardRepresentation board = gameService.makeMove(callbackData.getBoardId(), playerId, callbackData.getPitId());

      boolean isPlayerTwoTurn = board.getCurrentPlayer().equals(board.getPlayerTwo());

      Lobby lobby = lobbyService.getByBoardId(board.getId());
      EditMessageText sendPlayerOneMessage;
      EditMessageText sendPlayerTwoMessage;

      if (board.getGameStatus() != InProgress) {
        sendPlayerOneMessage = new EditMessageText()
            .setText(finalMessageForPlayerOne(board)[0])
            .setChatId(board.getPlayerOne().longValue())
            .setReplyMarkup(keyboardService.preparePlayerOneButtons(board))
            .setMessageId(lobby.getPlayerOneMessageId());

        sendPlayerTwoMessage = new EditMessageText()
            .setText(finalMessageForPlayerOne(board)[1])
            .setChatId(board.getPlayerTwo().longValue())
            .setReplyMarkup(keyboardService.preparePlayerTwoButtons(board))
            .setMessageId(lobby.getPlayerTwoMessageId());
      } else {
        sendPlayerOneMessage = new EditMessageText()
            .setText(isPlayerTwoTurn ? OPPONENTS_TURN_️MESSAGE : YOUR_TURN_MESSAGE)
            .setChatId(board.getPlayerOne().longValue())
            .setReplyMarkup(keyboardService.preparePlayerOneButtons(board))
            .setMessageId(lobby.getPlayerOneMessageId());

        sendPlayerTwoMessage = new EditMessageText()
            .setText(isPlayerTwoTurn ? YOUR_TURN_MESSAGE : OPPONENTS_TURN_️MESSAGE)
            .setChatId(board.getPlayerTwo().longValue())
            .setReplyMarkup(keyboardService.preparePlayerTwoButtons(board))
            .setMessageId(lobby.getPlayerTwoMessageId());
      }
      return List.of(sendPlayerOneMessage, sendPlayerTwoMessage);
    } catch (MakeMoveInFinishedGameException e) {
      return List.of(new AnswerCallbackQuery()
          .setText("Game ended!")
          .setCallbackQueryId(update.getCallbackQuery().getId()));
    }
  }

  private String[] finalMessageForPlayerOne(BoardRepresentation board) {
    GameStatus gameStatus = board.getGameStatus();
    switch (gameStatus) {
      case PlayerOneWins:
        return new String[]{PLAYER_WIN_MESSAGE, PLAYER_LOSE_MESSAGE};
      case PlayerTwoWins:
        return new String[]{PLAYER_LOSE_MESSAGE, PLAYER_WIN_MESSAGE};
      case Draw:
        return new String[]{DRAW_MESSAGE, DRAW_MESSAGE};
    }
    throw new IllegalStateException(String.format("Unhandled board state: %s", gameStatus));
  }
}
