package com.fivan.kalah.bot.handler.callback;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

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
