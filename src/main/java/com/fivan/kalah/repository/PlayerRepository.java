package com.fivan.kalah.repository;

import com.fivan.kalah.entity.Player;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerRepository {

  private Map<UUID, Player> players = new ConcurrentHashMap<>();

  public Player save(String name) {
    UUID id = UUID.randomUUID();
    Player player = new Player(id, name);
    players.put(id, player);
    return player;
  }

  public Optional<Player> getById(UUID id) {
    return Optional.ofNullable(players.get(id));
  }
}
