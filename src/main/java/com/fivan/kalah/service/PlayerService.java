package com.fivan.kalah.service;

import com.fivan.kalah.entity.Player;
import com.fivan.kalah.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository repository;

  public Player save(Player player) {
    return repository.save(player);
  }

  public Optional<Player> getById(Integer id) {
    return repository.getById(id);
  }

  public boolean playersExistsById(List<Integer> players) {
    return players.stream()
        .map(repository::getById)
        .allMatch(Optional::isPresent);
  }
}
