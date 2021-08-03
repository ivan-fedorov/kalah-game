package com.fivan.kalah.service;

import com.fivan.kalah.entity.Player;
import com.fivan.kalah.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository repository;

  public Player save(Player player) {
    return repository.save(player);
  }

  public Optional<Player> getById(Integer id) {
    return repository.findById(id);
  }

  public boolean playersExistsById(List<Integer> players) {
    return players.stream()
        .map(repository::findById)
        .allMatch(Optional::isPresent);
  }
}
