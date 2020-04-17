package com.fivan.kalah.entity;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class Player {

  @NotNull
  Integer id;

  @NotBlank
  String name;
}
