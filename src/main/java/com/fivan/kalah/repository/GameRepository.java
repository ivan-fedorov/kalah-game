package com.fivan.kalah.repository;

import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Board;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class GameRepository {

  private Map<UUID, BoardRepresentation> games = new ConcurrentHashMap<>();

  public BoardRepresentation addGame(Integer player1, Integer player2, int fieldSize, int stonesInPit) {
    Board board = new Board(player1, player2, fieldSize, stonesInPit);
    BoardRepresentation boardRepresentation = board.toRepresentation();
    games.putIfAbsent(boardRepresentation.getId(), boardRepresentation);
    return boardRepresentation;
  }

  public synchronized void update(BoardRepresentation board) {
    UUID boardId = board.getId();
    if (games.containsKey(boardId)) {
      games.put(boardId, board);
    } else {
      throw new IllegalStateException("Game with id: " + boardId + " not found");
    }
  }

  public Optional<BoardRepresentation> getById(UUID id) {
    return Optional.ofNullable(games.get(id));
  }

  public List<BoardRepresentation> getAll() {
    return List.copyOf(games.values());
  }

  public List<BoardRepresentation> getAllGamesById(Integer playerId) {
    return games.values().stream()
        .filter(boardRepresentation -> boardRepresentation.isParticipant(playerId))
        .collect(Collectors.toUnmodifiableList());
  }
}
