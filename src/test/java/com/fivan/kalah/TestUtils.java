package com.fivan.kalah;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Board;
import com.fivan.kalah.entity.Player;

import java.util.UUID;

public class TestUtils {

  private static ObjectMapper objectMapper = new ObjectMapper();

  public static String asJsonString(Object toJson) {
    try {
      return objectMapper.writeValueAsString(toJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static Player createPlayer() {
    return new Player(UUID.randomUUID(), "playerName");
  }

  public static BoardRepresentation createBoard(UUID playerOne, UUID playerTwo) {
    return new Board(playerOne, playerTwo, 7, 6).toRepresentation();
  }
}
