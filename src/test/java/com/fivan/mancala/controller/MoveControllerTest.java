package com.fivan.mancala.controller;

import com.fivan.mancala.dto.BoardRepresentation;
import com.fivan.mancala.entity.Player;
import com.fivan.mancala.filter.MancalaHeader;
import com.fivan.mancala.repository.GameRepository;
import com.fivan.mancala.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.UUID;

import static com.fivan.mancala.TestUtils.asJsonString;
import static com.fivan.mancala.TestUtils.createBoard;
import static com.fivan.mancala.TestUtils.createPlayer;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MoveControllerTest {

  private static final Player PLAYER_ONE = createPlayer();
  private static final UUID PLAYER_ONE_ID = PLAYER_ONE.getId();
  private static final Player PLAYER_TWO = createPlayer();
  private static final UUID PLAYER_TWO_ID = PLAYER_TWO.getId();

  private static final BoardRepresentation BOARD = createBoard(PLAYER_ONE_ID, PLAYER_TWO_ID);


  @Autowired private MockMvc mockMvc;
  @MockBean private PlayerRepository playerRepository;
  @MockBean private GameRepository gameRepository;

  @BeforeEach
  void setUp() {
    when(playerRepository.getById(eq(PLAYER_ONE_ID))).thenReturn(of(PLAYER_ONE));
  }

  @Test
  void createGame_should_createGame() throws Exception {
    when(gameRepository.getById(any(UUID.class))).thenReturn(of(BOARD));

    BoardRepresentation expected = new BoardRepresentation(BOARD.getId(), BOARD.getPlayerOne(), BOARD.getPlayerTwo(),
        new Integer[]{0, 7, 7, 7, 7, 7, 1, 0, 6, 6, 6, 6, 6, 6}, BOARD.getGameStatus(), BOARD.getCurrentPlayer());

    mockMvc.perform(post("/v1/games/" + BOARD.getId() + "/move")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"pitIndex\": 0 }")
        .header(MancalaHeader.PLAYER_ID, PLAYER_ONE_ID)
        .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(asJsonString(expected)));

    verify(gameRepository).getById(eq(BOARD.getId()));
  }

  @Test
  void createGame_should_throwException_when_headerIsNotPresent() throws Exception {
    MvcResult mvcResult = mockMvc.perform(post("/v1/games/" + BOARD.getId() + "/move")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNotFound())
        .andReturn();

    assertThat(mvcResult.getResolvedException()).isInstanceOf(MissingRequestHeaderException.class);
    verifyNoInteractions(gameRepository);
  }

  @Test
  void createGame_should_return404_when_playerIdIsNotFound() throws Exception {
    mockMvc.perform(post("/v1/games/" + BOARD.getId() + "/move")
        .header(MancalaHeader.PLAYER_ID, UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
        .andExpect(status().isNotFound());

    verifyNoInteractions(gameRepository);
  }
}