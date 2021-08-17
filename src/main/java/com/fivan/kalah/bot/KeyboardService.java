package com.fivan.kalah.bot;

import com.fivan.kalah.bot.handler.callback.CallbackDataFactory;
import com.fivan.kalah.bot.handler.callback.MakeMoveCallbackData;
import com.fivan.kalah.dto.BoardRepresentation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyboardService {

  private static final String NON_OPERATIONAL = "no-op";
  private final CallbackDataFactory callbackDataFactory;

  public InlineKeyboardMarkup preparePlayerOneButtons(BoardRepresentation board) {
    List<InlineKeyboardButton> playerOneRow = fillPlayerOneRow(board.getPlayerOne(), board, false);
    List<InlineKeyboardButton> playerTwoRow = fillPlayerTwoRow(board.getPlayerTwo(), board, false);

    InlineKeyboardMarkup playerOneKeyboardMarkup = new InlineKeyboardMarkup();
    playerOneKeyboardMarkup.setKeyboard(List.of(playerTwoRow, playerOneRow));
    return playerOneKeyboardMarkup;
  }

  public InlineKeyboardMarkup preparePlayerTwoButtons(BoardRepresentation board) {
    List<InlineKeyboardButton> playerTwoRow = fillPlayerTwoRow(board.getPlayerTwo(), board, true);
    List<InlineKeyboardButton> playerOneRow = fillPlayerOneRow(board.getPlayerOne(), board, true);

    InlineKeyboardMarkup playerOneKeyboardMarkup = new InlineKeyboardMarkup();
    playerOneKeyboardMarkup.setKeyboard(List.of(playerOneRow, playerTwoRow));
    return playerOneKeyboardMarkup;
  }

  private List<InlineKeyboardButton> fillPlayerOneRow(
      Integer playerId, BoardRepresentation board, boolean isPlayerTwo) {
    LinkedList<InlineKeyboardButton> playerOneRow = new LinkedList<>();

    List<Integer> playerOnePits = board.getPlayerPits(playerId);

    for (int i = 0; i < playerOnePits.size(); i++) {
      Integer pit = playerOnePits.get(i);
      InlineKeyboardButton pitButton =
          new InlineKeyboardButton()
              .setText(pit.toString())
              .setCallbackData(
                  isPlayerTwo ? NON_OPERATIONAL : callbackDataForMove(board.getId(), i));
      playerOneRow.add(pitButton);
    }
    playerOneRow.addFirst(new InlineKeyboardButton().setText("-").setCallbackData(NON_OPERATIONAL));

    if (isPlayerTwo) {
      Collections.reverse(playerOneRow);
    }

    return playerOneRow;
  }

  private List<InlineKeyboardButton> fillPlayerTwoRow(
      Integer playerId, BoardRepresentation board, boolean isPlayerTwo) {
    LinkedList<InlineKeyboardButton> playerTwoRow = new LinkedList<>();

    List<Integer> playerTwoPits = board.getPlayerPits(playerId);

    int pitSize = playerTwoPits.size() - 1;
    for (int i = 0; i <= pitSize; i++) {
      Integer pit = playerTwoPits.get(i);
      InlineKeyboardButton pitButton =
          new InlineKeyboardButton()
              .setText(pit.toString())
              .setCallbackData(
                  isPlayerTwo ? callbackDataForMove(board.getId(), pitSize - i) : NON_OPERATIONAL);
      playerTwoRow.addFirst(pitButton);
    }
    playerTwoRow.addFirst(new InlineKeyboardButton().setText("-").setCallbackData(NON_OPERATIONAL));

    if (!isPlayerTwo) {
      Collections.reverse(playerTwoRow);
    }

    return playerTwoRow;
  }

  private String callbackDataForMove(UUID boardId, int pitId) {
    MakeMoveCallbackData callbackData =
        MakeMoveCallbackData.builder().boardId(boardId).pitId(pitId).build();
    return callbackDataFactory.toStringRepresentation(callbackData);
  }
}
