package com.fivan.kalah.entity;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.util.UUID;

@Value
public class Player {

  @Null
  UUID id;

  @NotBlank
  String name;
}
