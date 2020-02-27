package com.fivan.mancala.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fivan.mancala.entity.GameStatus;
import com.fivan.mancala.util.GameUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
@Getter
@EqualsAndHashCode
public class BoardRepresentation {
  private final UUID id;
  private final UUID playerOne;
  private final UUID playerTwo;
  private final List<Integer> playerOnePits;
  private final List<Integer> playerTwoPits;
  private final GameStatus gameStatus;
  private final UUID currentPlayer;

  @JsonIgnore
  private final int fieldSize;

  public BoardRepresentation(UUID id, UUID playerOne, UUID playerTwo, Integer[] board,
                             GameStatus gameStatus, UUID currentPlayer) {
    this.id = id;
    this.playerOne = playerOne;
    this.playerTwo = playerTwo;
    this.fieldSize = board.length / 2;
    this.playerOnePits = Arrays.stream(board, 0, fieldSize)
        .collect(toUnmodifiableList());
    this.playerTwoPits = unmodifiableList(GameUtils.playerTwoList(board, fieldSize));
    this.gameStatus = gameStatus;
    this.currentPlayer = currentPlayer;
  }

  public String toString() {
    return '\n' + GameUtils.listToString(playerTwoPits) + "\n    " + GameUtils.listToString(playerOnePits);
  }

  public boolean isParticipant(UUID playerId) {
    return playerOne.equals(playerId) || playerTwo.equals(playerId);
  }
}
