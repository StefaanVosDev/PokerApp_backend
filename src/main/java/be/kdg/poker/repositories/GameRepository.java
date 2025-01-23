package be.kdg.poker.repositories;

import be.kdg.poker.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    @Query("""
    SELECT g
    FROM Game g
    LEFT JOIN FETCH g.players
    """)
    List<Game> findAllGamesWithPlayers();

    @Query("SELECT g FROM Game g LEFT JOIN FETCH g.rounds WHERE g.id = :gameId")
    Optional<Game> findByIdWithRounds(UUID gameId);

    @Query("SELECT g FROM Game g LEFT JOIN FETCH g.players WHERE g.id = :gameId")
    Optional<Game> findByIdWithPlayers(UUID gameId);

    @Query("""
            SELECT g
            FROM Game g
            LEFT JOIN g.rounds r
            WHERE r.id = :roundId
            """)
    Optional<Game> findByRoundId(UUID roundId);

    @Query("""
    SELECT g
    FROM Game g
    LEFT JOIN FETCH g.settings
    WHERE g.id = :id
    """)
    Optional<Game> findByIdWithSettings(UUID id);

    @Query("""
    SELECT g
    FROM Game g
    LEFT JOIN FETCH g.settings
    """)
    List<Game> findAllWithSettings();

    @Query("SELECT g FROM Game g LEFT JOIN FETCH g.messages WHERE g.id = :id")
    Optional<Game> findByIdWithMessages(UUID id);

    @Query("SELECT g FROM Game g JOIN g.players p WHERE p.id = :playerId")
    Optional<Game> findGameByPlayerId(UUID playerId);
}
