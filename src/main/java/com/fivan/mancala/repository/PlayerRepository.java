package com.fivan.mancala.repository;

import com.fivan.mancala.entity.Player;
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
