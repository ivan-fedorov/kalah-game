package com.fivan.mancala.service;

import com.fivan.mancala.entity.Player;
import com.fivan.mancala.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository repository;

  public Player save(String name) {
    return repository.save(name);
  }

  public Player getById(UUID id) {
    return repository.getById(id).orElseThrow();
  }

  public boolean playersExistsById(List<UUID> players) {
    return players.stream()
        .map(repository::getById)
        .allMatch(Optional::isPresent);
  }
}
