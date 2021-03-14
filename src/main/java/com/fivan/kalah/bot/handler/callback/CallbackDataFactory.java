package com.fivan.kalah.bot.handler.callback;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CallbackDataFactory {

  private static final String DELIMITER = ":";

  public String toStringRepresentation(CallbackData callbackData) {
    return String.join(DELIMITER, callbackData.elements());
  }

  public MakeMoveCallbackData toMakeMoveCallbackData(String callback) {
    String[] split = callback.split(DELIMITER);
    return new MakeMoveCallbackData(UUID.fromString(split[1]), Integer.valueOf(split[2]));
  }

}
