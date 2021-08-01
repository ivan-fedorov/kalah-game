package com.fivan.kalah.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BoardTest {

  private static final Player FIRST_PLAYER = new Player(1, "John");
  private static final Player SECOND_PLAYER = new Player(2, "Paul");
  private Board board;

  @Test
  void positiveTest() {
    board = new Board(FIRST_PLAYER.getId(), SECOND_PLAYER.getId(), 7, 6);

    var actual = board.makeMove(0, FIRST_PLAYER.getId());

    assertThat(actual.getPlayerTwoPits()).containsExactly(0, 6, 6, 6, 6, 6, 6);
    assertThat(actual.getPlayerOnePits()).containsExactly(0, 7, 7, 7, 7, 7, 1);

    assertThat(actual.getCurrentPlayer()).isEqualTo(FIRST_PLAYER.getId());
  }

  @Test
  void positive_secondPlayerMove() {
    board = new Board(FIRST_PLAYER.getId(), SECOND_PLAYER.getId(), 7, 4);

    var actual = board.makeMove(0, FIRST_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(0, 4, 4, 4, 4, 4, 4);
    assertThat(actual.getPlayerOnePits()).containsExactly(0, 5, 5, 5, 5, 4, 0);
    assertThat(actual.getCurrentPlayer()).isEqualTo(SECOND_PLAYER.getId());

    actual = board.makeMove(2, SECOND_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(1, 5, 5, 5, 0, 4, 4);
    assertThat(actual.getPlayerOnePits()).containsExactly(0, 5, 5, 5, 5, 4, 0);
    assertThat(actual.getCurrentPlayer()).isEqualTo(SECOND_PLAYER.getId());
  }

  @Test
  void positive_pitOnOwnSideIsEmpty_forFirstPlayer() {
    board = new Board(FIRST_PLAYER.getId(), SECOND_PLAYER.getId(), 7, 3);
    var actual = board.makeMove(3, FIRST_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(0, 3, 3, 3, 3, 3, 3);
    assertThat(actual.getPlayerOnePits()).containsExactly(3, 3, 3, 0, 4, 4, 1);
    assertThat(actual.getCurrentPlayer()).isEqualTo(FIRST_PLAYER.getId());

    actual = board.makeMove(0, FIRST_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(0, 3, 3, 3, 0, 3, 3);
    assertThat(actual.getPlayerOnePits()).containsExactly(0, 4, 4, 0, 4, 4, 5);
    assertThat(actual.getCurrentPlayer()).isEqualTo(SECOND_PLAYER.getId());
  }

  @Test
  void positive_pitOnOwnSideIsEmpty_forSecondPlayer() {
    board = new Board(FIRST_PLAYER.getId(), SECOND_PLAYER.getId(), 7, 3);
    var actual = board.makeMove(0, FIRST_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(0, 3, 3, 3, 3, 3, 3);
    assertThat(actual.getPlayerOnePits()).containsExactly(0, 4, 4, 4, 3, 3, 0);
    assertThat(actual.getCurrentPlayer()).isEqualTo(SECOND_PLAYER.getId());

    actual = board.makeMove(3, SECOND_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(1, 4, 4, 0, 3, 3, 3);
    assertThat(actual.getPlayerOnePits()).containsExactly(0, 4, 4, 4, 3, 3, 0);
    assertThat(actual.getCurrentPlayer()).isEqualTo(SECOND_PLAYER.getId());

    actual = board.makeMove(0, SECOND_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(6, 4, 4, 0, 4, 4, 0);
    assertThat(actual.getPlayerOnePits()).containsExactly(0, 4, 0, 4, 3, 3, 0);
    assertThat(actual.getCurrentPlayer()).isEqualTo(FIRST_PLAYER.getId());
  }

  @Test
  void should_skip_opponents_bigPit_on_playerOne_move() {
    board = new Board(FIRST_PLAYER.getId(), SECOND_PLAYER.getId(), 4, 5);
    var actual = board.makeMove(2, FIRST_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(0, 6, 6, 6);
    assertThat(actual.getPlayerOnePits()).containsExactly(6, 5, 0, 1);
  }

  @Test
  void should_skip_opponents_bigPit_on_playerTwo_move() {
    board = new Board(FIRST_PLAYER.getId(), SECOND_PLAYER.getId(), 4, 5);
    var actual = board.makeMove(2, FIRST_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(0, 6, 6, 6);
    assertThat(actual.getPlayerOnePits()).containsExactly(6, 5, 0, 1);

    actual = board.makeMove(2, SECOND_PLAYER.getId());
    assertThat(actual.getPlayerTwoPits()).containsExactly(1, 0, 7, 7);
    assertThat(actual.getPlayerOnePits()).containsExactly(7, 6, 1, 1);
  }
}