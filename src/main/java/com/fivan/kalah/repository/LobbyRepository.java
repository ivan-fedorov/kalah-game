package com.fivan.kalah.repository;

import com.fivan.kalah.entity.Lobby;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LobbyRepository extends CrudRepository<Lobby, UUID> {

  Optional<Lobby> findByBoardId(UUID boardId);

  Optional<Lobby> findByIdAndBoardIdIsNull(UUID id);

  List<Lobby> findTop5ByPlayerOne_RatingGreaterThanEqualOrderByPlayerOne_RatingAsc(Integer rating);

  List<Lobby> findTop5ByPlayerOne_RatingLessThanEqualOrderByPlayerOne_RatingDesc(Integer rating);
}
