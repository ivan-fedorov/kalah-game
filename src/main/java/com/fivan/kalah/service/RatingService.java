package com.fivan.kalah.service;

import com.fivan.kalah.entity.GameStatus;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.repository.PlayerRepository;
import com.fivan.kalah.service.RatingCalculator.RatingCalculation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {

  private final PlayerRepository playerRepository;
  private final RatingCalculator ratingCalculator;

  /**
   * Calculates new rating for both players and persist it. Rating is calculated by the rules
   * described here: https://rttf.ru/content/2
   */
  public void updateRating(Integer playerOneId, Integer playerTwoId, GameStatus status) {
    Player playerOne = playerRepository.findById(playerOneId).orElseThrow();
    Player playerTwo = playerRepository.findById(playerTwoId).orElseThrow();

    RatingCalculation calculation = ratingCalculator.calculate(playerOne.getRating(),
        playerTwo.getRating(), status);

    playerRepository.save(playerOne.withRating(calculation.getPlayerOneRating()));
    playerRepository.save(playerTwo.withRating(calculation.getPlayerTwoRating()));
  }

}
