package com.fivan.kalah.controller;

import com.fivan.kalah.TestUtils;
import com.fivan.kalah.entity.Player;
import com.fivan.kalah.filter.MancalaHeader;
import com.fivan.kalah.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.UUID;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
class PlayerControllerTest {

  private static final Player PLAYER = TestUtils.createPlayer();

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private PlayerRepository playerRepository;

  @Test
  void save_should_savePlayer() throws Exception {
    when(playerRepository.save(anyString())).thenReturn(PLAYER);

    mockMvc.perform(post("/v1/players")
        .content("{ \"name\" : \"playerName\" }")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(content().json(TestUtils.asJsonString(PLAYER)));
  }

  @Test
  void getById_should_returnPlayer() throws Exception {
    when(playerRepository.getById(any(UUID.class))).thenReturn(of(PLAYER));

    UUID playerId = PLAYER.getId();
    mockMvc.perform(get("/v1/players/" + playerId)
        .contentType(MediaType.APPLICATION_JSON)
        .header(MancalaHeader.PLAYER_ID, playerId)
        .accept(MediaType.APPLICATION_JSON)
    )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(TestUtils.asJsonString(PLAYER)));
  }

  @Test
  void getById_should_throwException_when_headerIsNotPresent() throws Exception {
    UUID playerId = PLAYER.getId();

    MvcResult mvcResult = mockMvc.perform(get("/v1/players/" + playerId)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNotFound())
        .andReturn();

    assertThat(mvcResult.getResolvedException()).isInstanceOf(MissingRequestHeaderException.class);
  }

  @Test
  void getById_should_return404_when_playerIdIsNotFound() throws Exception {
    UUID playerId = PLAYER.getId();

    mockMvc.perform(get("/v1/players/" + playerId)
        .header(MancalaHeader.PLAYER_ID, playerId)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
        .andExpect(status().isNotFound());
  }
}