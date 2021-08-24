package com.fivan.kalah.repository;

import com.fivan.kalah.entity.PlayerState;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerStateRepository extends CrudRepository<PlayerState, Integer> {
}
