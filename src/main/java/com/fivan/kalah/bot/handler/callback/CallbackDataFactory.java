package com.fivan.kalah.bot.handler.callback;

import org.springframework.stereotype.Component;

@Component
public class CallbackDataFactory {

  private static final String DELIMITER = ":";

  public String toStringRepresentation(CallbackData callbackData) {
    return String.join(DELIMITER, callbackData.elements());
  }

}
