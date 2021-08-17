package com.fivan.kalah.repository;

import com.fivan.kalah.dto.BoardRepresentation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameRepository extends CrudRepository<BoardRepresentation, UUID> {}
