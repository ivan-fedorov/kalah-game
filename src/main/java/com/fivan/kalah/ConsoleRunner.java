package com.fivan.kalah;

import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Board;
import com.fivan.kalah.entity.Player;

import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import static com.fivan.kalah.entity.GameStatus.InProgress;

public class ConsoleRunner {

  private static final Player FIRST_PLAYER = new Player(UUID.randomUUID(), "Paul");
  private static final Player SECOND_PLAYER = new Player(UUID.randomUUID(), "John");

  private static final Map<UUID, Player> PLAYER_BY_ID = Map.of(FIRST_PLAYER.getId(), FIRST_PLAYER,
      SECOND_PLAYER.getId(), SECOND_PLAYER);

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Please set up field size and stones in pit");
    int fieldSize = scanner.nextInt();
    int stonesInPit = scanner.nextInt();
    Board board = new Board(FIRST_PLAYER.getId(), SECOND_PLAYER.getId(), fieldSize, stonesInPit);

    System.out.println("Board was generated");

    Player currentPlayer = FIRST_PLAYER;
    BoardRepresentation representation = null;
    while (representation == null || InProgress.equals(representation.getGameStatus())) {
      System.out.println(currentPlayer + " moves");
      int pit = scanner.nextInt();
      try {
        representation = board.makeMove(pit, currentPlayer.getId());
        currentPlayer = PLAYER_BY_ID.get(representation.getCurrentPlayer());
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
    System.out.println();
    System.out.println(representation.getGameStatus());
    System.out.println(representation.toString());
  }
}
