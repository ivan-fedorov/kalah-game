package com.fivan.kalah.bot;

import lombok.Builder;
import lombok.Value;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.List;

@Value
@Builder
public class HandlingResult {
  Event event;
  @Builder.Default
  List<BotApiMethod<?>> methods = List.of();
  @Builder.Default
  List<LobbySendMessageAction> actions = List.of();
}
