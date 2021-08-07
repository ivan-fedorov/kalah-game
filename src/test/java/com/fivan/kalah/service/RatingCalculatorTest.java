package com.fivan.kalah.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fivan.kalah.entity.GameStatus;
import com.fivan.kalah.service.RatingCalculator.RatingCalculation;
import org.junit.jupiter.api.Test;

public class RatingCalculatorTest {

  private final RatingCalculator ratingCalculator = new RatingCalculator();

  @Test
  void hundredPointsAddedToPlayerOneWhenInitialRatingIsEqualAndPlayerOneWins() {
    RatingCalculation calculation = ratingCalculator.calculate(1000, 1000, GameStatus.PlayerOneWins);

    assertThat(calculation.getPlayerOneRating()).isEqualTo(1100);
    assertThat(calculation.getPlayerTwoRating()).isEqualTo(900);
  }

  @Test
  void hundredPointsAddedToPlayerTwoWhenInitialRatingIsEqualAndPlayerTwoWins() {
    RatingCalculation calculation = ratingCalculator.calculate(1000, 1000, GameStatus.PlayerTwoWins);

    assertThat(calculation.getPlayerOneRating()).isEqualTo(900);
    assertThat(calculation.getPlayerTwoRating()).isEqualTo(1100);
  }

  @Test
  void ratingIsNotChangedWhenGameStatusIsDrawAndInitialRatingGapIsHundredOrLess() {
    RatingCalculation calculation = ratingCalculator.calculate(1000, 1000, GameStatus.Draw);

    assertThat(calculation.getPlayerOneRating()).isEqualTo(1000);
    assertThat(calculation.getPlayerTwoRating()).isEqualTo(1000);
  }

  @Test
  void ratingIsRecalculatedWhenGameStatusIsDrawAndInitialRatingGapIsMoreThanHundred() {
    RatingCalculation calculation = ratingCalculator.calculate(1101, 1000, GameStatus.Draw);

    assertThat(calculation.getPlayerOneRating()).isEqualTo(1065);
    assertThat(calculation.getPlayerTwoRating()).isEqualTo(1036);
  }

  @Test
  void ratingIsNotChangedWhenWinnerRatingIsHigherThanLoserRatingOnMoreThanHundredPoints() {
    RatingCalculation calculation = ratingCalculator.calculate(1101, 1000, GameStatus.PlayerOneWins);

    assertThat(calculation.getPlayerOneRating()).isEqualTo(1101);
    assertThat(calculation.getPlayerTwoRating()).isEqualTo(1000);
  }

  @Test
  void ratingIsChangedWhenLoserRatingIsLowerThanWinnerRatingOnMoreThanHundredPoints() {
    RatingCalculation calculation = ratingCalculator.calculate(1101, 1000, GameStatus.PlayerTwoWins);

    assertThat(calculation.getPlayerOneRating()).isEqualTo(991);
    assertThat(calculation.getPlayerTwoRating()).isEqualTo(1110);
  }

  @Test
  void illegalArgumentExceptionIsThrownWhenGameStatusIsInProgress() {
    assertThatThrownBy(() -> ratingCalculator.calculate(1000, 1000, GameStatus.InProgress))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage( "Rating should not be updated for the games that are in progress");
  }

}
