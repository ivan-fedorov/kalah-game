package com.fivan.kalah.entity;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Value
@Builder
public class Lobby {
  @Id UUID id;
  Integer playerId;
  @With Integer playerOneMessageId;
  @With Integer playerTwoMessageId;
  @With UUID boardId;
  @With Player playerOne;
  @With Player playerTwo;
}
