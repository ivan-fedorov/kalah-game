package com.fivan.kalah.service;

import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.repository.LobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LobbyService {

  private final LobbyRepository repository;

  public Lobby createLobby(Lobby lobby) {
    return repository.save(lobby);
  }

  public Optional<Lobby> getById(UUID id) {
    return repository.getById(id);
  }
}
