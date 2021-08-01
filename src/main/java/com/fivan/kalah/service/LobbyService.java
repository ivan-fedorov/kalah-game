package com.fivan.kalah.service;

import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.repository.LobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LobbyService {

  private final LobbyRepository repository;

  public Lobby createLobby(Lobby lobby) {
    return repository.save(lobby);
  }

  public void addPlayerOneMessageId(UUID lobbyId, Integer messageId) {
    repository.addPlayerOneMessageId(lobbyId, messageId);
  }

  public void addPlayerTwoMessageId(UUID lobbyId, Integer messageId) {
    repository.addPlayerTwoMessageId(lobbyId, messageId);
  }

  public Optional<Lobby> getById(UUID id) {
    return repository.getById(id);
  }

  public void addBoardId(UUID lobbyId, UUID boardId) {
    repository.addBoardId(lobbyId, boardId);
  }

  public Lobby getByBoardId(UUID boardId) {
    return repository.getByBoardId(boardId)
        .orElseThrow();
  }
}
