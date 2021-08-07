package com.fivan.kalah.service;

import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.repository.LobbyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {

  private final LobbyRepository repository;

  public Lobby createLobby(Lobby lobby) {
    return repository.save(lobby);
  }

  public void addPlayerOneMessageId(UUID lobbyId, Integer messageId) {
    manageUpdate(lobbyId, lobby -> lobby.withPlayerOneMessageId(messageId));
  }

  public void addPlayerTwoMessageId(UUID lobbyId, Integer messageId) {
    manageUpdate(lobbyId, lobby -> lobby.withPlayerTwoMessageId(messageId));
  }

  public Optional<Lobby> findById(UUID id) {
    return repository.findById(id);
  }

  public void addBoardId(UUID lobbyId, UUID boardId) {
    manageUpdate(lobbyId, lobby -> lobby.withBoardId(boardId));
  }

  public Lobby getByBoardId(UUID boardId) {
    return repository.findByBoardId(boardId)
        .orElseThrow();
  }

  private void manageUpdate(UUID lobbyId, Function<Lobby, Lobby> mapper) {
    repository.findById(lobbyId).map(mapper)
        .ifPresentOrElse(repository::save, () -> log.warn("Lobby with ID='{}' wasn't found", lobbyId));
  }
}