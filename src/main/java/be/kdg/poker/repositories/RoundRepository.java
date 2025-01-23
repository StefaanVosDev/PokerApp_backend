package be.kdg.poker.repositories;

import be.kdg.poker.domain.Card;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {
    @Query("SELECT r FROM Round r LEFT JOIN FETCH r.communityCards WHERE r.game = :game ORDER BY r.createdAt DESC LIMIT 1")
    Optional<Round> findByGameWithCommunityCards(@Param("game") Game game);

    @Query("SELECT r FROM Round r LEFT JOIN FETCH r.deck WHERE r.game = :game ORDER BY r.createdAt DESC LIMIT 1")
    Optional<Round> findByGameWithDeck(@Param("game") Game game);

    @Query("""
            SELECT r
            FROM Round r
            LEFT OUTER JOIN FETCH r.turns
            WHERE r.game.id = :gameId
            ORDER BY r.createdAt DESC
            LIMIT 1
            """)
    Optional<Round> findLatestByGameWithTurns(UUID gameId);

    @Query("""
    SELECT r
    FROM Round r
    LEFT JOIN FETCH r.deck
    WHERE r.game.id = :gameId
    ORDER BY r.createdAt DESC
    LIMIT 1
    """)
    Optional<Round> findLatestByGameWithDeck(UUID gameId);

    @Query("""
    SELECT r
    FROM Round r
    WHERE r.game.id = :gameId
    ORDER BY r.createdAt DESC
    LIMIT 1
    """)
    Optional<Round> findLatestByGame(UUID gameId);

    @Query("""
    SELECT r
    FROM Round r
    LEFT JOIN FETCH r.communityCards
    WHERE r.game.id = :gameId
    ORDER BY r.createdAt DESC
    LIMIT 1
    """)
    Optional<Round> findLatestByGameIdWithCommunityCards(UUID gameId);

    @Query("SELECT r FROM Round r LEFT JOIN FETCH r.turns WHERE r.id = :roundId")
    Optional<Round> findByIdWithTurns(UUID roundId);

    @Query("SELECT r.deck FROM Round r WHERE r.id = :roundId")
    List<Card> findDeckByRoundId(@Param("roundId") UUID roundId);

    @Query("""
    SELECT r
    FROM Round r
    LEFT JOIN FETCH r.communityCards
    WHERE r.id = :id
    """)
    Optional<Round> findByIdWithCommunityCards(UUID id);

    @Query("SELECT r FROM Round r LEFT JOIN FETCH r.game WHERE r.id = :id")
    Optional<Round> findByIdWithGame(UUID id);

    @Query("SELECT r FROM Round r LEFT JOIN FETCH r.game WHERE r.game.id = :id")
    List<Round> findAllByGameId(UUID id);
}