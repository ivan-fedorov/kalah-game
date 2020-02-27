package com.fivan.mancala.controller;

import com.fivan.mancala.entity.Player;
import com.fivan.mancala.service.PlayerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@RestController
@RequestMapping("/v1/players")
@RequiredArgsConstructor
public class PlayerController {

  private final PlayerService service;

  @PostMapping
  public ResponseEntity<Player> save(@RequestBody PlayerRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request.getName()));
  }

  @GetMapping("/{id}")
  public Player getById(@RequestHeader("player-id") UUID playerId, @PathVariable UUID id) {
    return service.getById(id);
  }

  @Data
  private static class PlayerRequest {
    @NotBlank private String name;
  }


}
