package be.kdg.poker.repositories;

import be.kdg.poker.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.hand WHERE p.id = :playerId")
    Optional<Player> findByIdWithHand(UUID playerId);

    @Query("SELECT p FROM Player p WHERE p.game.id = :gameId")
    List<Player> findAllByGameId(UUID gameId);

    @Query("""
    SELECT p
    FROM Player p
    LEFT JOIN p.game g
    LEFT JOIN FETCH p.hand
    WHERE g.id = :gameId
    """)
    List<Player> findByGameIdWithHands(UUID gameId);

    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.account WHERE p.id = :playerId")
    Optional<Player> findByIdWithAccount(UUID playerId);

    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.game WHERE p.id = :playerId")
    Optional<Player> findByIdWithGame(UUID playerId);

    @Query("SELECT p FROM Player p WHERE p.account.id = :accountId AND p.game.id = :gameId")
    Optional<Player> findByAccountAndGame(UUID accountId, UUID gameId);
}
