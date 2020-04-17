package com.fivan.kalah.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

@UtilityClass
public class GameUtils {

  public static String listToString(List<Integer> list) {
    return list.stream().map(n -> String.format("%02d", n)).collect(joining(", ", "[", "]"));
  }

  public static List<Integer> playerTwoList(Integer[] board, int fieldSize) {
    List<Integer> playerTwoList = new ArrayList<>();
    for (int j = fieldSize * 2 - 1; j > fieldSize - 1; j--) {
      playerTwoList.add(board[j]);
    }
    return unmodifiableList(playerTwoList);
  }

  public static Integer getUserIdFromMessage(Update update) {
    return update.getMessage().getFrom().getId();
  }

}
