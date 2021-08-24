package com.fivan.kalah.repository;

import com.fivan.kalah.dto.BoardRepresentation;
import com.fivan.kalah.entity.GameStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class GameStatisticsRepository {
  private final MongoTemplate mongoTemplate;

  public Map<Integer, Integer> gamesByPlayerId(Set<Integer> playerIds) {
    List<Map> playerOneCount = countPlayerGames(playerIds, "playerOne");
    List<Map> playerTwoCount = countPlayerGames(playerIds, "playerTwo");
    return Stream.concat(playerOneCount.stream(), playerTwoCount.stream())
        .map(map -> (Map<Integer, Integer>) map)
        .collect(
            Collectors.toUnmodifiableMap(map -> map.get("_id"), map -> map.get("total"), Integer::sum));
  }

  private List<Map> countPlayerGames(Set<Integer> playerIds, String fieldName) {
    MatchOperation playerMatch =
        Aggregation.match(
            new Criteria()
                .andOperator(
                    Criteria.where(fieldName).in(playerIds),
                    Criteria.where("gameStatus").ne(GameStatus.InProgress)));
    GroupOperation totalPlayerGames = Aggregation.group(fieldName).count().as("total");

    return mongoTemplate
        .aggregate(
            Aggregation.newAggregation(playerMatch, totalPlayerGames),
            BoardRepresentation.class,
            Map.class)
        .getMappedResults();
  }
}
