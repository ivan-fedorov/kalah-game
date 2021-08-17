package com.fivan.kalah.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import javax.validation.constraints.NotBlank;

@Value
@Builder
@AllArgsConstructor
public class Player {

  @Id Integer id;

  @NotBlank String name;

  @With Integer rating;

  @With @Version Integer version;

  public static Player player(Integer id, String name) {
    return new Player(id, name, 1000, null);
  }
}
