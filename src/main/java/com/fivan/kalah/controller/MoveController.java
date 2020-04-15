package com.fivan.kalah.controller;

import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.service.GameService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.UUID;

@RestController
@RequestMapping("/v1/games/{gameId}/move")
@RequiredArgsConstructor
public class MoveController {

  private final GameService gameService;

  @PostMapping
  public BoardRepresentation move(@RequestHeader("player-id") UUID playerId, @PathVariable UUID gameId,
                                  @RequestBody MoveRequest body) {
    return gameService.makeMove(gameId, playerId, body.getPitIndex());
  }

  @Data
  private static class MoveRequest {

    @NotNull
    @PositiveOrZero
    private Integer pitIndex;
  }
}
