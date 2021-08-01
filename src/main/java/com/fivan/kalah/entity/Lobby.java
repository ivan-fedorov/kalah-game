package com.fivan.kalah.entity;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.UUID;

@Value
@Builder
public class Lobby {
  UUID id;
  Integer playerId;
  @With
  Integer playerOneMessageId;
  @With
  Integer playerTwoMessageId;
  @With
  UUID boardId;
}
