package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.LobbySendMessageAction;
import lombok.Builder;
import lombok.Value;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.List;

@Value
@Builder
public class ActionsAndMethods {
  @Builder.Default List<BotApiMethod<?>> methods = List.of();
  @Builder.Default List<LobbySendMessageAction> actions = List.of();
}
