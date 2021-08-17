package com.fivan.kalah.bot.handler.callback;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class MakeMoveCallbackData implements CallbackData {

  UUID boardId;
  Integer pitId;

  @Override
  public List<String> elements() {
    return List.of(CallbackType.MAKE_MOVE.getPrefix(), boardId.toString(), pitId.toString());
  }
}
