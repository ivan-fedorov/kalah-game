package com.fivan.kalah.service;

import com.fivan.kalah.entity.GameStatus;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RatingService {

  private final PlayerRepository playerRepository;

  /**
   * Calculates new rating for both players and persist it. Rating is calculated by the rules
   * described here: https://rttf.ru/content/2
   */
  public void updateRating(Integer playerOneId, Integer playerTwoId, GameStatus status) {
    if (status == GameStatus.InProgress) {
      throw new IllegalArgumentException(
          "Rating should not be updated for the games that are in progress");
    }

    Player playerOne = playerRepository.findById(playerOneId).orElseThrow();
    Player playerTwo = playerRepository.findById(playerTwoId).orElseThrow();

    if (status == GameStatus.Draw) {
      return;
    }

    Player winner = status == GameStatus.PlayerOneWins ? playerOne : playerTwo;
    Player loser = status == GameStatus.PlayerTwoWins ? playerOne : playerTwo;

    if (winner.getRating() - loser.getRating() > 100) {
      return;
    }

    int delta = ((100 - (winner.getRating() - loser.getRating()) / 10));

    playerRepository.save(winner.withRating(winner.getRating() + delta));
    int potentialLoserRating = loser.getRating() - delta;
    playerRepository.save(loser.withRating(Math.max(potentialLoserRating, 1)));
  }

}
