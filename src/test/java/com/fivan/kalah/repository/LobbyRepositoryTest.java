package com.fivan.kalah.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fivan.kalah.entity.Lobby;
import com.fivan.kalah.entity.Player;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

@DataMongoTest
class LobbyRepositoryTest {

  @Autowired private LobbyRepository lobbyRepository;

  @AfterEach
  void tearDown() {
    lobbyRepository.deleteAll();
  }

  @Test
  void findByPlayerOne_RatingGreaterThanOrderByPlayerOne_RatingAsc() {
    for (int i = 0; i < 10; i++) {
      Player player = Player.builder().id(i).rating(999 + i).name("Tester").build();
      lobbyRepository.save(
          Lobby.builder().playerId(player.getId()).playerOne(player).id(UUID.randomUUID()).build());
    }

    List<Lobby> result =
        lobbyRepository.findTop5ByPlayerOne_RatingGreaterThanOrderByPlayerOne_RatingAsc(1000);

    assertThat(result)
        .hasSize(5)
        .isSortedAccordingTo(Comparator.comparing(lobby -> lobby.getPlayerOne().getRating()))
        .last()
        .extracting(lobby -> lobby.getPlayerOne().getRating())
        .isEqualTo(1005);
  }

  @Test
  void findTop5ByPlayerOne_RatingLessThanEqualOrderByPlayerOne_RatingDesc() {
    for (int i = 0; i < 10; i++) {
      Player player = Player.builder().id(i).rating(1001 - i).name("Tester").build();
      lobbyRepository.save(
          Lobby.builder().playerId(player.getId()).playerOne(player).id(UUID.randomUUID()).build());
    }

    List<Lobby> result =
        lobbyRepository.findTop5ByPlayerOne_RatingLessThanOrderByPlayerOne_RatingDesc(1000);

    assertThat(result)
        .hasSize(5)
        .isSortedAccordingTo(
            Comparator.comparing((Lobby lobby) -> lobby.getPlayerOne().getRating()).reversed())
        .last()
        .extracting(lobby -> lobby.getPlayerOne().getRating())
        .isEqualTo(995);
  }

  @Test
  void findTop10ByPlayerOne_Rating() {
    List<Integer> ratings =
        IntStream.concat(
                IntStream.concat(IntStream.range(990, 1000), IntStream.range(1001, 1010)),
                IntStream.range(0, 11).map(i -> 1000))
            .boxed()
            .collect(Collectors.toList());
    for (int rating : ratings) {
      Player player = Player.builder().id(rating).rating(rating).name("Tester").build();
      lobbyRepository.save(
          Lobby.builder().playerId(player.getId()).playerOne(player).id(UUID.randomUUID()).build());
    }

    List<Lobby> result = lobbyRepository.findTop10ByPlayerOne_Rating(1000);

    assertThat(result)
        .hasSize(10)
        .allSatisfy(lobby -> assertThat(lobby.getPlayerOne().getRating()).isEqualTo(1000));
  }
}
