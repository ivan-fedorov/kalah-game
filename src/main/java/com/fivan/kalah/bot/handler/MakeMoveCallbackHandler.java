package com.fivan.kalah.bot.handler;

import static com.fivan.kalah.entity.GameStatus.InProgress;

import com.fivan.kalah.bot.KeyboardService;
import com.fivan.kalah.bot.handler.callback.CallbackDataFactory;
import com.fivan.kalah.bot.handler.callback.CallbackType;
import com.fivan.kalah.bot.handler.callback.MakeMoveCallbackData;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.GameStatus;
import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.exception.MakeMoveInFinishedGameException;
import com.fivan.kalah.service.GameService;
import com.fivan.kalah.service.GameService.MakeMoveResult;
import com.fivan.kalah.service.LobbyService;
import com.fivan.kalah.service.RatingCalculator.RatingCalculation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class MakeMoveCallbackHandler {

  private static final String YOUR_TURN_MESSAGE = "Your turn ⬇️";
  private static final String OPPONENTS_TURN_MESSAGE = "Opponents turn ⬆️";
  private static final String PLAYER_WIN_MESSAGE = "Chicken dinner, you are winner \uD83D\uDC51. Your rating is %d (%d)";
  private static final String PLAYER_LOSE_MESSAGE = "You lose! Good day sir \uD83D\uDE14. Your rating is %d (%d)";
  private static final String DRAW_MESSAGE = "DRAW \uD83E\uDD1D. Your rating is %d (%d)";
  private final GameService gameService;
  private final CallbackDataFactory callbackDataFactory;
  private final LobbyService lobbyService;
  private final KeyboardService keyboardService;

  public boolean canProcess(Update update) {
    return update.getCallbackQuery() != null && update.getCallbackQuery().getData()
        .startsWith(CallbackType.MAKE_MOVE.getPrefix());
  }

  public List<BotApiMethod<?>> handle(Update update, Integer playerId) {
    MakeMoveCallbackData callbackData = callbackDataFactory.toMakeMoveCallbackData(
        update.getCallbackQuery().getData());
    try {
      MakeMoveResult moveResult = gameService.makeMove(callbackData.getBoardId(), playerId,
          callbackData.getPitId());
      BoardRepresentation board = moveResult.getBoard();

      boolean isPlayerTwoTurn = board.getCurrentPlayer().equals(board.getPlayerTwo());

      Lobby lobby = lobbyService.getByBoardId(board.getId());
      EditMessageText sendPlayerOneMessage;
      EditMessageText sendPlayerTwoMessage;

      if (board.getGameStatus() != InProgress) {
        sendPlayerOneMessage = new EditMessageText()
            .setText(finalMessageForPlayerOne(moveResult)[0])
            .setChatId(board.getPlayerOne().longValue())
            .setReplyMarkup(keyboardService.preparePlayerOneButtons(board))
            .setMessageId(lobby.getPlayerOneMessageId());

        sendPlayerTwoMessage = new EditMessageText()
            .setText(finalMessageForPlayerOne(moveResult)[1])
            .setChatId(board.getPlayerTwo().longValue())
            .setReplyMarkup(keyboardService.preparePlayerTwoButtons(board))
            .setMessageId(lobby.getPlayerTwoMessageId());
      } else {
        sendPlayerOneMessage = new EditMessageText()
            .setText(isPlayerTwoTurn ? OPPONENTS_TURN_MESSAGE : YOUR_TURN_MESSAGE)
            .setChatId(board.getPlayerOne().longValue())
            .setReplyMarkup(keyboardService.preparePlayerOneButtons(board))
            .setMessageId(lobby.getPlayerOneMessageId());

        sendPlayerTwoMessage = new EditMessageText()
            .setText(isPlayerTwoTurn ? YOUR_TURN_MESSAGE : OPPONENTS_TURN_MESSAGE)
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

  private String[] finalMessageForPlayerOne(MakeMoveResult result) {
    GameStatus gameStatus = result.getBoard().getGameStatus();
    RatingCalculation rating = result.getRatingCalculation();
    switch (gameStatus) {
      case PlayerOneWins:
        return new String[]{
            String.format(PLAYER_WIN_MESSAGE, rating.getPlayerOneRating(),
                rating.getPlayerOneDelta()),
            String.format(PLAYER_LOSE_MESSAGE, rating.getPlayerTwoRating(),
                rating.getPlayerTwoDelta())};
      case PlayerTwoWins:
        return new String[]{
            String.format(PLAYER_LOSE_MESSAGE, rating.getPlayerOneRating(),
                rating.getPlayerOneDelta()),
            String.format(PLAYER_WIN_MESSAGE, rating.getPlayerTwoRating(),
                rating.getPlayerTwoDelta())};
      case Draw:
        return new String[]{
            String.format(DRAW_MESSAGE, rating.getPlayerOneRating(), rating.getPlayerOneDelta()),
            String.format(DRAW_MESSAGE, rating.getPlayerTwoRating(), rating.getPlayerTwoDelta())};
    }
    throw new IllegalStateException(String.format("Unhandled board state: %s", gameStatus));
  }
}
