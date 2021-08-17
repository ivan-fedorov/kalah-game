package com.fivan.kalah.service;

import com.fivan.kalah.entity.GameStatus;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BinaryOperator;

@Slf4j
@Component
public class RatingCalculator {

  public RatingCalculation calculate(
      Integer playerOneRating, Integer playerTwoRating, GameStatus status) {
    if (status == GameStatus.InProgress) {
      throw new IllegalArgumentException(
          "Rating should not be updated for the games that are in progress");
    }

    if (status == GameStatus.Draw) {
      return calculateDrawRatings(playerOneRating, playerTwoRating);
    }

    WinnerLoserRating ratings = resolveWinnerLoserRatings(status, playerOneRating, playerTwoRating);
    if (ratings.getWinnerRating() - ratings.getLoserRating() > 100) {
      return RatingCalculation.builder()
          .playerOneDelta(0)
          .playerTwoDelta(0)
          .playerOneRating(playerOneRating)
          .playerTwoRating(playerTwoRating)
          .build();
    }

    return calculateWinLoseRatings(
        status,
        ratings.getWinnerRating(),
        ratings.getLoserRating(),
        this::calculateWinLoseRatingDelta);
  }

  private RatingCalculation calculateWinLoseRatings(
      GameStatus status,
      int winnerRating,
      int loserRating,
      BinaryOperator<Integer> deltaCalculator) {
    int delta = deltaCalculator.apply(winnerRating, loserRating);
    int potentialLoserRating = loserRating - delta;
    int newLoserRating = Math.max(potentialLoserRating, 1);
    int newWinnerRating = winnerRating + delta;

    int playerOneRating = status == GameStatus.PlayerOneWins ? newWinnerRating : newLoserRating;
    int playerTwoRating = status == GameStatus.PlayerTwoWins ? newWinnerRating : newLoserRating;

    return RatingCalculation.builder()
        .playerOneDelta(status == GameStatus.PlayerOneWins ? delta : newLoserRating - loserRating)
        .playerTwoDelta(status == GameStatus.PlayerTwoWins ? delta : newLoserRating - loserRating)
        .playerOneRating(playerOneRating)
        .playerTwoRating(playerTwoRating)
        .build();
  }

  private RatingCalculation calculateDrawRatings(int playerOneRating, int playerTwoRating) {
    int initialGap = Math.abs(playerOneRating - playerTwoRating);
    if (initialGap <= 100) {
      return RatingCalculation.builder()
          .playerOneDelta(0)
          .playerTwoDelta(0)
          .playerOneRating(playerOneRating)
          .playerTwoRating(playerTwoRating)
          .build();
    }

    log.debug("Draw calculation initial gap: {}", initialGap);
    GameStatus pseudoStatus =
        playerOneRating > playerTwoRating ? GameStatus.PlayerTwoWins : GameStatus.PlayerOneWins;

    WinnerLoserRating ratings =
        resolveWinnerLoserRatings(pseudoStatus, playerOneRating, playerTwoRating);
    return calculateWinLoseRatings(
        pseudoStatus,
        ratings.getWinnerRating(),
        ratings.getLoserRating(),
        (winnerRating, loserRating) -> calculateWinLoseRatingDelta(winnerRating, loserRating) / 3);
  }

  private WinnerLoserRating resolveWinnerLoserRatings(
      GameStatus status, int playerOneRating, int playerTwoRating) {
    int winnerRating = status == GameStatus.PlayerOneWins ? playerOneRating : playerTwoRating;
    int loserRating = status == GameStatus.PlayerTwoWins ? playerOneRating : playerTwoRating;
    return new WinnerLoserRating(winnerRating, loserRating);
  }

  private int calculateWinLoseRatingDelta(int winnerRating, int loserRating) {
    return (100 - (winnerRating - loserRating)) / 10;
  }

  @Value
  @Builder
  public static class RatingCalculation {
    @NonNull Integer playerOneDelta;
    @NonNull Integer playerTwoDelta;
    @NonNull Integer playerOneRating;
    @NonNull Integer playerTwoRating;
  }

  @Value
  private static class WinnerLoserRating {
    int winnerRating;
    int loserRating;
  }
}
