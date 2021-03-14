package com.fivan.kalah.bot;

import com.fivan.kalah.bot.handler.callback.CallbackDataFactory;
import com.fivan.kalah.bot.handler.callback.MakeMoveCallbackData;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Lobby;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyboardService {

  private final CallbackDataFactory callbackDataFactory;

  public List<SendMessage> prepareMessages(Integer playerId, Integer opponentPlayerId, BoardRepresentation board) {
    InlineKeyboardMarkup playerKeyboardMarkup = getPlayerKeyboardMarkup(playerId, opponentPlayerId, board);
    InlineKeyboardMarkup opponentKeyboardMarkup = getOpponentKeyboardMarkup(playerId, opponentPlayerId, board);

    SendMessage sendPlayerMessage = new SendMessage()
        .setText("Opponent move")
        .setChatId(playerId.longValue())
        .setReplyMarkup(playerKeyboardMarkup);

    SendMessage sendOpponentMessage = new SendMessage()
        .setText("Take turn")
        .setChatId(opponentPlayerId.longValue())
        .setReplyMarkup(opponentKeyboardMarkup);
    return List.of(sendPlayerMessage, sendOpponentMessage);
  }

  private InlineKeyboardMarkup getPlayerKeyboardMarkup(Integer playerId, Integer opponentPlayerId, BoardRepresentation board) {
    InlineKeyboardMarkup playerKeyboardMarkup = new InlineKeyboardMarkup();
    List<InlineKeyboardButton> currentPlayerRow = fillCurrentPlayerRow(playerId, board, false);
    List<InlineKeyboardButton> opponentPlayerRow = fillOpponentPlayerRow(opponentPlayerId, board, false);
    playerKeyboardMarkup.setKeyboard(List.of(opponentPlayerRow, currentPlayerRow));
    return playerKeyboardMarkup;
  }

  private InlineKeyboardMarkup getOpponentKeyboardMarkup(Integer playerId, Integer opponentPlayerId, BoardRepresentation board) {
    InlineKeyboardMarkup playerKeyboardMarkup = new InlineKeyboardMarkup();
    List<InlineKeyboardButton> currentPlayerRow = fillCurrentPlayerRow(opponentPlayerId, board, true);
    List<InlineKeyboardButton> opponentPlayerRow = fillOpponentPlayerRow(playerId, board, true);
    playerKeyboardMarkup.setKeyboard(List.of(opponentPlayerRow, currentPlayerRow));
    return playerKeyboardMarkup;
  }

  private List<InlineKeyboardButton> fillCurrentPlayerRow(Integer playerId, BoardRepresentation board, boolean isPlayerOne) {
    LinkedList<InlineKeyboardButton> currentPlayerRow = new LinkedList<>();

    List<Integer> playerPits = board.getPlayerPits(playerId);

    int pitSize = playerPits.size() - 1;
    for (int i = 0; i <= pitSize; i++) {
      Integer pit = playerPits.get(i);
      InlineKeyboardButton pitButton = new InlineKeyboardButton()
          .setText(pit.toString())
          .setCallbackData(callbackDataForMove(board.getId(), i));
      currentPlayerRow.addFirst(pitButton);
    }

    if (isPlayerOne) {
      Collections.reverse(currentPlayerRow);
    }

    currentPlayerRow.addFirst(new InlineKeyboardButton().setText("-").setCallbackData("random"));
    return currentPlayerRow;
  }

  private String callbackDataForMove(UUID boardId, int pitId) {
    MakeMoveCallbackData callbackData = MakeMoveCallbackData.builder()
        .boardId(boardId)
        .pitId(pitId)
        .build();
    return callbackDataFactory.toStringRepresentation(callbackData);
  }

  private List<InlineKeyboardButton> fillOpponentPlayerRow(Integer playerId, BoardRepresentation board, boolean isPlayerOne) {
    LinkedList<InlineKeyboardButton> currentPlayerRow = new LinkedList<>();

    List<Integer> playerPits = board.getPlayerPits(playerId);

    for (Integer pit : playerPits) {
      InlineKeyboardButton pitButton = new InlineKeyboardButton()
          .setText(pit.toString())
          .setCallbackData("no-op");

      currentPlayerRow.addFirst(pitButton);
    }

    if (isPlayerOne) {
      Collections.reverse(currentPlayerRow);
    }

    currentPlayerRow.add(new InlineKeyboardButton().setText("-").setCallbackData("no-op"));

    return currentPlayerRow;
  }
}
