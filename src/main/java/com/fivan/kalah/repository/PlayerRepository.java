package com.fivan.kalah.repository;

import com.fivan.kalah.entity.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Integer> {

  List<Player> findByOrderByRatingDesc(Pageable pageable);
}
