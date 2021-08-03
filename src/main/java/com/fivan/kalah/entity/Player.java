package com.fivan.kalah.entity;

import lombok.Value;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class Player {

  @Id
  Integer id;

  @NotBlank
  String name;
}
