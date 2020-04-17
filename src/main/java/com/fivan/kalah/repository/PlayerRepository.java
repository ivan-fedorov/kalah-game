package com.fivan.kalah.repository;

import com.fivan.kalah.entity.Player;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerRepository {

  private Map<Integer, Player> players = new ConcurrentHashMap<>();

  public Player save(Player player) {
    players.put(player.getId(), player);
    return player;
  }

  public Optional<Player> getById(Integer id) {
    return Optional.ofNullable(players.get(id));
  }
}
