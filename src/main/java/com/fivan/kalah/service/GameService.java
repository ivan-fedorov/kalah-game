package com.fivan.kalah.service;

import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Board;
import com.fivan.kalah.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

  public static final int DEFAULT_FIELD_SIZE = 7;
  public static final int DEFAULT_STONES_IN_PIT = 6;

  private final GameRepository repository;
  private final PlayerService playerService;

  public BoardRepresentation createGame(Integer player1, Integer player2) {
    if (player1.equals(player2)) {
      throw new IllegalStateException("Can't create game with two same players: " + player1);
    }

    if (!playerService.playersExistsById(List.of(player1, player2))) {
      throw new IllegalStateException("Players weren't found");
    }

    return repository.addGame(player1, player2, DEFAULT_FIELD_SIZE, DEFAULT_STONES_IN_PIT);
  }

  public BoardRepresentation makeMove(UUID gameId, Integer playerId, Integer pitIndex) {
    BoardRepresentation boardRepresentation = getById(gameId, playerId);
    BoardRepresentation afterMove = Board.fromRepresentation(boardRepresentation).makeMove(pitIndex, playerId);
    repository.update(afterMove);
    return afterMove;
  }

  public BoardRepresentation getById(UUID id, Integer playerId) {
    return repository.getById(id)
        .filter(boardRepresentation -> boardRepresentation.isParticipant(playerId))
        .orElseThrow();
  }

  public List<BoardRepresentation> getActiveGamesById(Integer playerId) {
    return repository.getAllGamesById(playerId);
  }
}
