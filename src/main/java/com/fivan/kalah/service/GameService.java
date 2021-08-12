package com.fivan.kalah.service;

import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Board;
import com.fivan.kalah.entity.GameStatus;
import com.fivan.kalah.repository.GameRepository;
import com.fivan.kalah.service.RatingCalculator.RatingCalculation;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

  public static final int DEFAULT_FIELD_SIZE = 7;
  public static final int DEFAULT_STONES_IN_PIT = 6;

  private final GameRepository repository;
  private final PlayerService playerService;
  private final RatingService ratingService;

  public BoardRepresentation createGame(Integer player1, Integer player2) {
    if (player1.equals(player2)) {
      throw new IllegalStateException("Can't create game with two same players: " + player1);
    }

    if (!playerService.playersExistsById(List.of(player1, player2))) {
      throw new IllegalStateException("Players weren't found");
    }

    Board board = new Board(player1, player2, DEFAULT_FIELD_SIZE, DEFAULT_STONES_IN_PIT);
    return repository.save(board.toRepresentation());
  }

  public MakeMoveResult makeMove(UUID gameId, Integer playerId, Integer pitIndex) {
    BoardRepresentation boardRepresentation = getById(gameId, playerId);
    BoardRepresentation afterMove = Board.fromRepresentation(boardRepresentation)
        .makeMove(pitIndex, playerId);
    BoardRepresentation savedUpdatedBoard = repository.save(afterMove);
    RatingCalculation ratingCalculation = null;
    if (afterMove.getGameStatus() != GameStatus.InProgress) {
      ratingCalculation = ratingService.updateRating(
          boardRepresentation.getPlayerOne(),
          boardRepresentation.getPlayerTwo(),
          afterMove.getGameStatus()
      );
    }
    return MakeMoveResult.builder()
        .board(savedUpdatedBoard)
        .ratingCalculation(ratingCalculation)
        .build();
  }

  public BoardRepresentation getById(UUID id, Integer playerId) {
    return repository.findById(id)
        .filter(boardRepresentation -> boardRepresentation.isParticipant(playerId))
        .orElseThrow();
  }

  @Value
  @Builder
  public static class MakeMoveResult {
    BoardRepresentation board;
    RatingCalculation ratingCalculation;
  }
}
