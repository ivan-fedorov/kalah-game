package com.fivan.kalah.entity;

import lombok.Value;

import java.util.UUID;

@Value
public class Lobby {
  UUID id;
  Integer playerId;
}
