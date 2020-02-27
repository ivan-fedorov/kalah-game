package com.fivan.mancala.entity;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.util.UUID;

@Value
public class Player {

  @Null
  private UUID id;

  @NotBlank
  private String name;
}
