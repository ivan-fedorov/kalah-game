package com.fivan.mancala.filter;

import com.fivan.mancala.entity.Player;
import com.fivan.mancala.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

  private final PlayerRepository playerRepository;
  private Set<String> allowedUrls = Set.of("/v2/api-docs", "/configuration/.*", "/swagger.*", "/webjars/.*");

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return allowedUrls.stream().anyMatch(url -> request.getRequestURI().matches(url)) ||
        (request.getRequestURI().equals("/v1/players") && request.getMethod().equals("POST"));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    Optional<Player> player = Optional.ofNullable(request.getHeader(MancalaHeader.PLAYER_ID))
        .flatMap(id -> playerRepository.getById(UUID.fromString(id)));
    if (player.isEmpty()) {
      response.sendError(404, "Player not found");
    }
    filterChain.doFilter(request, response);
  }

}
