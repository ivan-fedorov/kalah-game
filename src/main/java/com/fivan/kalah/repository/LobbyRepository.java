package com.fivan.kalah.repository;

import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.entity.Player;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LobbyRepository {

  private Map<UUID, Lobby> lobbies = new ConcurrentHashMap<>();

  public Lobby save(Lobby lobby) {
    lobbies.put(lobby.getId(), lobby);
    return lobby;
  }

  public Optional<Lobby> getById(UUID id) {
    return Optional.ofNullable(lobbies.get(id));
  }
}
