package com.fivan.kalah.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.entity.Player;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

@DataMongoTest
class LobbyRepositoryTest {

  @Autowired
  private LobbyRepository lobbyRepository;

  @Test
  void findByPlayerOne_RatingGreaterThanOrderByPlayerOne_RatingAsc() {
    for (int i = 0; i < 10; i++) {
      Player player = Player.builder()
          .id(i)
          .rating(999 + i)
          .name("Tester")
          .build();
      lobbyRepository.save(
          Lobby.builder()
              .playerId(player.getId())
              .playerOne(player)
              .id(UUID.randomUUID())
              .build()
      );
    }

    List<Lobby> result = lobbyRepository.findTop5ByPlayerOne_RatingGreaterThanEqualOrderByPlayerOne_RatingAsc(
        1000);

    assertThat(result).hasSize(5);
    assertThat(result).isSortedAccordingTo(
        Comparator.comparing(lobby -> lobby.getPlayerOne().getRating()));
    assertThat(result).last().extracting(lobby -> lobby.getPlayerOne().getRating()).isEqualTo(1004);
    lobbyRepository.deleteAll();
  }

  @Test
  void findTop5ByPlayerOne_RatingLessThanEqualOrderByPlayerOne_RatingDesc() {
    for (int i = 0; i < 10; i++) {
      Player player = Player.builder()
          .id(i)
          .rating(1001 - i)
          .name("Tester")
          .build();
      lobbyRepository.save(
          Lobby.builder()
              .playerId(player.getId())
              .playerOne(player)
              .id(UUID.randomUUID())
              .build()
      );
    }

    List<Lobby> result = lobbyRepository.findTop5ByPlayerOne_RatingLessThanEqualOrderByPlayerOne_RatingDesc(
        1000);

    assertThat(result).hasSize(5);
    assertThat(result).isSortedAccordingTo(
        Comparator.comparing((Lobby lobby) -> lobby.getPlayerOne().getRating()).reversed());
    assertThat(result).last().extracting(lobby -> lobby.getPlayerOne().getRating()).isEqualTo(996);
    lobbyRepository.deleteAll();
  }
}