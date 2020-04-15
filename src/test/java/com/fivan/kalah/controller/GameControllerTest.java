package com.fivan.kalah.controller;


import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.filter.MancalaHeader;
import com.fivan.kalah.repository.GameRepository;
import com.fivan.kalah.repository.PlayerRepository;
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

import java.util.List;
import java.util.UUID;

import static com.fivan.kalah.TestUtils.asJsonString;
import static com.fivan.kalah.TestUtils.createBoard;
import static com.fivan.kalah.TestUtils.createPlayer;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

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
    when(playerRepository.getById(eq(PLAYER_TWO_ID))).thenReturn(of(PLAYER_TWO));
    when(gameRepository.addGame(any(UUID.class), any(UUID.class), anyInt(), anyInt())).thenReturn(BOARD);
  }

  @Test
  void createGame_should_createGame() throws Exception {
    mockMvc.perform(post("/v1/games/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"playerOne\":\"" + PLAYER_ONE_ID + "\", \"playerTwo\":\"" + PLAYER_TWO_ID + "\" }")
        .header(MancalaHeader.PLAYER_ID, PLAYER_ONE_ID)
        .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
        .andExpect(status().isCreated())
        .andExpect(content().json(asJsonString(BOARD)));
  }

  @Test
  void getActiveGames_should_returnListOfCreatedGames() throws Exception {
    List<BoardRepresentation> expectedList = List.of(createBoard(UUID.randomUUID(), UUID.randomUUID()),
        createBoard(UUID.randomUUID(), UUID.randomUUID()));
    when(gameRepository.getAllGamesById(any(UUID.class))).thenReturn(expectedList);

    mockMvc.perform(get("/v1/games/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(MancalaHeader.PLAYER_ID, PLAYER_ONE_ID)
        .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(asJsonString(expectedList)));

    verify(gameRepository).getAllGamesById(eq(PLAYER_ONE_ID));
  }

  @Test
  void createGame_should_throwException_when_headerIsNotPresent() throws Exception {
    MvcResult mvcResult = mockMvc.perform(post("/v1/games/")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNotFound())
        .andReturn();

    assertThat(mvcResult.getResolvedException()).isInstanceOf(MissingRequestHeaderException.class);
    verifyNoInteractions(gameRepository);
  }

  @Test
  void createGame_should_return404_when_playerIdIsNotFound() throws Exception {
    mockMvc.perform(post("/v1/games/")
        .header(MancalaHeader.PLAYER_ID, UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
        .andExpect(status().isNotFound());

    verifyNoInteractions(gameRepository);
  }

  @Test
  void getActiveGames_should_throwException_when_headerIsNotPresent() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/v1/games/")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNotFound())
        .andReturn();

    assertThat(mvcResult.getResolvedException()).isInstanceOf(MissingRequestHeaderException.class);
    verifyNoInteractions(gameRepository);
  }

  @Test
  void getActiveGames_should_return404_when_playerIdIsNotFound() throws Exception {
    mockMvc.perform(get("/v1/games/")
        .header(MancalaHeader.PLAYER_ID, UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
        .andExpect(status().isNotFound());
  }
}