package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.KeyboardService;
import com.fivan.kalah.bot.handler.callback.CallbackDataFactory;
import com.fivan.kalah.bot.handler.callback.CallbackType;
import com.fivan.kalah.bot.handler.callback.MakeMoveCallbackData;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MakeMoveCallbackHandler {
  private final GameService gameService;
  private final CallbackDataFactory callbackDataFactory;
  private final KeyboardService keyboardService;

  public boolean canProcess(Update update) {
    return update.getCallbackQuery() != null && update.getCallbackQuery().getData().startsWith(CallbackType.MAKE_MOVE.getPrefix());
  }

  public List<BotApiMethod<?>> handle(Update update, Integer playerId) {
    MakeMoveCallbackData callbackData = callbackDataFactory.toMakeMoveCallbackData(update.getCallbackQuery().getData());
    BoardRepresentation board = gameService.makeMove(callbackData.getBoardId(), playerId, callbackData.getPitId());

    boolean isPlayerTwoTurn = board.getCurrentPlayer().equals(board.getPlayerTwo());
    SendMessage sendPlayerOneMessage = new SendMessage()
        .setText(isPlayerTwoTurn ? "Opponents turn" : "Your turn")
        .setChatId(board.getPlayerOne().longValue())
        .setReplyMarkup(keyboardService.preparePlayerOneButtons(board));

    SendMessage sendPlayerTwoMessage = new SendMessage()
        .setText(isPlayerTwoTurn ? "Your turn" : "Opponents turn")
        .setChatId(board.getPlayerTwo().longValue())
        .setReplyMarkup(keyboardService.preparePlayerTwoButtons(board));

    return List.of(sendPlayerOneMessage, sendPlayerTwoMessage);
  }
}
