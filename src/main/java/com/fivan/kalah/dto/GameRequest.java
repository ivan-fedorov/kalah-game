package com.fivan.kalah.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GameRequest {
  private UUID playerOne;
  private UUID playerTwo;
}
