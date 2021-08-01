package com.fivan.kalah.repository;

import com.fivan.kalah.entity.Lobby;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LobbyRepository {

  private final Map<UUID, Lobby> lobbies = new ConcurrentHashMap<>();

  public Lobby save(Lobby lobby) {
    lobbies.put(lobby.getId(), lobby);
    return lobby;
  }

  public void addPlayerOneMessageId(UUID lobbyId, Integer messageId) {
    lobbies.computeIfPresent(lobbyId, (id, lobby) ->
        lobby.withPlayerOneMessageId(messageId)
    );
  }

  public void addPlayerTwoMessageId(UUID lobbyId, Integer messageId) {
    lobbies.computeIfPresent(lobbyId, (id, lobby) ->
        lobby.withPlayerTwoMessageId(messageId)
    );
  }

  public Optional<Lobby> getById(UUID id) {
    return Optional.ofNullable(lobbies.get(id));
  }

  public void addBoardId(UUID lobbyId, UUID boardId) {
    lobbies.computeIfPresent(lobbyId, (id, lobby) ->
        lobby.withBoardId(boardId)
    );
  }

  public Optional<Lobby> getByBoardId(UUID boardId) {
    return lobbies.values().stream()
        .filter(lobby -> boardId.equals(lobby.getBoardId()))
        .findAny();
  }
}
