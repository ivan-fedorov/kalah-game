package com.fivan.kalah.bot.handler.callback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Getter
@RequiredArgsConstructor
public enum CallbackType {
  MAKE_MOVE("makeMove");

  private static final Map<String, CallbackType> typesByPrefix =
      Arrays.stream(CallbackType.values())
          .collect(toMap(CallbackType::getPrefix, Function.identity()));

  private final String prefix;
}
