package com.fivan.kalah.service;

import com.fivan.kalah.entity.GameStatus;
import lombok.Builder;
import lombok.Value;
import org.springframework.stereotype.Component;

@Component
public class RatingCalculator {

  public RatingCalculation calculate(Integer playerOneRating, Integer playerTwoRating, GameStatus status) {
    if (status == GameStatus.InProgress) {
      throw new IllegalArgumentException(
          "Rating should not be updated for the games that are in progress");
    }

    if (status == GameStatus.Draw) {
      return RatingCalculation.builder()
          .playerOneRating(playerOneRating)
          .playerTwoRating(playerTwoRating)
          .build();
    }

    int winnerRating = status == GameStatus.PlayerOneWins ? playerOneRating : playerTwoRating;
    int loserRating = status == GameStatus.PlayerTwoWins ? playerOneRating : playerTwoRating;

    if (winnerRating - loserRating > 100) {
      return RatingCalculation.builder()
          .playerOneRating(playerOneRating)
          .playerTwoRating(playerTwoRating)
          .build();
    }

    int delta = ((100 - (winnerRating - loserRating) / 10));
    int potentialLoserRating = loserRating - delta;
    int newLoserRating = Math.max(potentialLoserRating, 1);
    int newWinnerRating = winnerRating + delta;

    return RatingCalculation.builder()
        .playerOneRating(status == GameStatus.PlayerOneWins ? newWinnerRating : newLoserRating)
        .playerTwoRating(status == GameStatus.PlayerTwoWins ? newWinnerRating : newLoserRating)
        .build();
  }

  @Value
  @Builder
  public static class RatingCalculation {
    int playerOneRating;
    int playerTwoRating;
  }
}
