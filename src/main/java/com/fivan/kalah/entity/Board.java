package com.fivan.kalah.entity;

import com.fivan.kalah.dto.BoardRepresentation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fivan.kalah.entity.Board.PlayerRole.PLAYER_ONE;
import static com.fivan.kalah.entity.Board.PlayerRole.PLAYER_TWO;
import static com.fivan.kalah.entity.GameStatus.Draw;
import static com.fivan.kalah.entity.GameStatus.InProgress;
import static com.fivan.kalah.entity.GameStatus.PlayerOneWins;
import static com.fivan.kalah.entity.GameStatus.PlayerTwoWins;
import static com.fivan.kalah.util.GameUtils.listToString;
import static com.fivan.kalah.util.GameUtils.playerTwoList;
import static java.util.Arrays.asList;

/**
 * Service that represents board and it operations.
 */
@Slf4j
public class Board {

  private final UUID id;
  private final Integer[] board;
  private final Map<UUID, PlayerRole> roleById;
  private final Map<PlayerRole, UUID> idByRole;
  private final int fieldSize;
  private final int lastPitIndex;
  private UUID currentPlayer;
  private GameStatus gameStatus;

  /**
   * Creates play board.
   *
   * @param player1     {@link UUID} of first {@link Player}.
   * @param player2     {@link UUID} of second {@link Player}.
   * @param fieldSize   number of pits (including big) for one player.
   * @param stonesInPit number of stones in one pit.
   */
  public Board(UUID player1, UUID player2, int fieldSize, int stonesInPit) {
    this.id = UUID.randomUUID();
    this.fieldSize = fieldSize;
    this.lastPitIndex = fieldSize - 1;
    this.roleById = Map.of(player1, PLAYER_ONE, player2, PLAYER_TWO);
    this.idByRole = Map.of(PLAYER_ONE, player1, PLAYER_TWO, player2);
    this.currentPlayer = player1;
    this.board = generateBoard(stonesInPit);
    this.gameStatus = InProgress;
    logBoardState();
  }

  private Board(UUID id, Integer[] board, UUID player1, UUID player2,
                int fieldSize, UUID currentPlayer, GameStatus gameStatus) {
    this.id = id;
    this.board = board;
    this.roleById = Map.of(player1, PLAYER_ONE, player2, PLAYER_TWO);
    this.idByRole = Map.of(PLAYER_ONE, player1, PLAYER_TWO, player2);
    this.fieldSize = fieldSize;
    this.currentPlayer = currentPlayer;
    this.gameStatus = gameStatus;
    this.lastPitIndex = fieldSize - 1;
  }

  public static Board fromRepresentation(BoardRepresentation br) {
    List<Integer> playerTwoPits = new ArrayList<>(br.getPlayerTwoPits());
    Collections.reverse(playerTwoPits);
    List<Integer> board = new ArrayList<>(br.getPlayerOnePits());
    board.addAll(playerTwoPits);
    return new Board(br.getId(), board.<Integer>toArray(new Integer[0]),
        br.getPlayerOne(), br.getPlayerTwo(),
        br.getFieldSize(), br.getCurrentPlayer(), br.getGameStatus());
  }

  public BoardRepresentation makeMove(int pitIndex, UUID playerId) {
    if (!gameStatus.equals(InProgress)) {
      throw new IllegalStateException("Game has been ended with status: " + gameStatus);
    }

    if (!playerId.equals(currentPlayer)) {
      throw new IllegalArgumentException("It's turn for " + currentPlayer);
    }

    if (board[pitIndex + ((roleById.get(currentPlayer).getPlayerIndex() - 1) * fieldSize)] == 0) {
      throw new IllegalArgumentException("It's 0 stones in current pit, choose another pit");
    }

    moveStones(pitIndex, roleById.get(currentPlayer));

    return validateWinConditionAndReturnRepresentation();
  }

  public BoardRepresentation toRepresentation() {
    return new BoardRepresentation(id, idByRole.get(PLAYER_ONE), idByRole.get(PLAYER_TWO),
        board, gameStatus, currentPlayer);
  }

  private void moveStones(int pitIndex, PlayerRole playerRole) {
    if (pitIndex >= lastPitIndex) {
      throw new IllegalArgumentException("Player can take stones only from 0 to " + (lastPitIndex - 1) + " pit");
    }
    int pitToEmpty = pitIndex + (playerRole.getPlayerIndex() - 1) * fieldSize;
    int stones = board[pitToEmpty];
    board[pitToEmpty] = 0;

    int currentPit = pitToEmpty + 1;
    for (int i = 0; i < stones; i++) {
      if (currentPit == getOtherPlayer(playerRole).getPlayerIndex() * lastPitIndex + getOtherPlayer(playerRole).getPlayerIndex() - 1) {
        i--;
        currentPit++;
        currentPit %= fieldSize * 2;
        continue;
      }
      board[currentPit]++;
      if (i < stones - 1) {
        currentPit++;
        currentPit %= fieldSize * 2;
      }
    }
    if (currentPit < getBigPit(playerRole) && fieldSize * (playerRole.getPlayerIndex() - 1) <= currentPit) {
      if (board[currentPit] == 1) {
        board[getBigPit(playerRole)] += board[lastPitIndex * 2 - currentPit] + 1;
        board[lastPitIndex * 2 - currentPit] = 0;
        board[currentPit] = 0;
      }
    }
    if (currentPit != getBigPit(playerRole)) {
      currentPlayer = idByRole.get(getOtherPlayer(currentPlayer));
    }
  }

  private BoardRepresentation validateWinConditionAndReturnRepresentation() {
    List<Integer> playerOneStones = asList(board).subList(0, fieldSize - 1);
    List<Integer> playerTwoStones = asList(board).subList(fieldSize, fieldSize * 2 - 1);

    if (isEmptyPits(playerOneStones) || isEmptyPits(playerTwoStones)) {
      if (board[lastPitIndex] > board[fieldSize * 2 - 1]) {
        log.info("First player won");
        gameStatus = PlayerOneWins;
      } else if (board[lastPitIndex] < board[fieldSize * 2 - 1]) {
        log.info("Second player won");
        gameStatus = PlayerTwoWins;
      } else {
        log.info("Draw");
        gameStatus = Draw;
      }
    }
    logBoardState();
    return toRepresentation();
  }

  private static boolean isEmptyPits(List<Integer> playerOneStones) {
    return playerOneStones.stream().allMatch(stones -> stones.equals(0));
  }

  private int getBigPit(PlayerRole playerRole) {
    return playerRole.getPlayerIndex() * fieldSize - 1;
  }

  private PlayerRole getOtherPlayer(UUID playerId) {
    return getOtherPlayer(roleById.get(playerId));
  }

  private PlayerRole getOtherPlayer(PlayerRole playerRole) {
    return playerRole == PLAYER_ONE ? PLAYER_TWO : PLAYER_ONE;
  }

  private Integer[] generateBoard(int stonesInPit) {
    int pitCount = fieldSize * 2;
    Integer[] board = new Integer[pitCount];
    for (int i = 0; i < pitCount; i++) {
      if (i == pitCount - 1 || i == fieldSize - 1) {
        board[i] = 0;
        continue;
      }
      board[i] = stonesInPit;
    }
    return board;
  }

  private void logBoardState() {
    log.info("Board: \n {} \n     {}",
        listToString(playerTwoList(board, fieldSize)),
        listToString(asList(board).subList(0, fieldSize)));
  }

  @RequiredArgsConstructor
  enum PlayerRole {
    PLAYER_ONE(1),
    PLAYER_TWO(2);

    @Getter
    private final int playerIndex;
  }

}
