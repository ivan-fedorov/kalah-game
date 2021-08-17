package com.fivan.kalah.service;

import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.entity.Player;
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

  public Optional<Lobby> findEmptyLobbyById(UUID id) {
    return repository.findByIdAndBoardIdIsNull(id);
  }

  public void handlePlayerTwoJoining(UUID lobbyId, UUID boardId, Player playerTwo) {
    manageUpdate(lobbyId, lobby -> lobby.withBoardId(boardId).withPlayerTwo(playerTwo));
  }

  public Lobby getByBoardId(UUID boardId) {
    return repository.findByBoardId(boardId).orElseThrow();
  }

  private void manageUpdate(UUID lobbyId, Function<Lobby, Lobby> mapper) {
    repository
        .findById(lobbyId)
        .map(mapper)
        .ifPresentOrElse(
            repository::save, () -> log.warn("Lobby with ID='{}' wasn't found", lobbyId));
  }
}
