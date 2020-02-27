package com.fivan.mancala.controller;

import com.fivan.mancala.dto.BoardRepresentation;
import com.fivan.mancala.dto.GameRequest;
import com.fivan.mancala.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/games")
public class GameController {

  private final GameService gameService;

  @PostMapping
  public ResponseEntity<BoardRepresentation> createGame(@RequestHeader("player-id") UUID playerId, @RequestBody GameRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(gameService.createGame(request.getPlayerOne(), request.getPlayerTwo()));
  }

  @GetMapping
  public List<BoardRepresentation> getActiveGamesById(@RequestHeader("player-id") UUID playerId) {
    return gameService.getActiveGamesById(playerId);
  }
}
