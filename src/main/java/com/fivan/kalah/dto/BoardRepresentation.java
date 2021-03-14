package com.fivan.kalah.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fivan.kalah.entity.GameStatus;
import com.fivan.kalah.util.GameUtils;
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
  private final Integer playerOne;
  private final Integer playerTwo;
  private final List<Integer> playerOnePits;
  private final List<Integer> playerTwoPits;
  private final GameStatus gameStatus;
  private final Integer currentPlayer;

  @JsonIgnore
  private final int fieldSize;

  public BoardRepresentation(UUID id, Integer playerOne, Integer playerTwo, Integer[] board,
                             GameStatus gameStatus, Integer currentPlayer) {
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

  public boolean isParticipant(Integer playerId) {
    return playerOne.equals(playerId) || playerTwo.equals(playerId);
  }

  public List<Integer> getPlayerPits(Integer playerId) {
    if (playerId.equals(playerOne)) {
      return playerOnePits;
    }
    if (playerId.equals(playerTwo)) {
      return playerTwoPits;
    }
    throw new IllegalStateException(
        String.format("Player with id: %s doesn't belong to game: %s", playerId, id));
  }

  public Integer getOpponentId(Integer playerId) {
    return playerOne.equals(playerId) ? playerTwo : playerOne;
  }
}
