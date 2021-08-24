package com.fivan.kalah.entity;

import com.fivan.kalah.bot.State;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

@Value
@AllArgsConstructor
public class PlayerState {
  @Id Integer playerId;
  @With State state;
  @With @Version Integer version;
}
