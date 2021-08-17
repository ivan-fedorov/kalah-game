package com.fivan.kalah.repository;

import com.fivan.kalah.entity.Lobby;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LobbyRepository extends CrudRepository<Lobby, UUID> {
  Optional<Lobby> findByBoardId(UUID boardId);

  Optional<Lobby> findByIdAndBoardIdIsNull(UUID id);
}
