package be.kdg.poker.repositories;

import be.kdg.poker.domain.Turn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TurnRepository extends JpaRepository<Turn, UUID> {

    @Query("SELECT t FROM Turn t LEFT JOIN FETCH t.round WHERE t.id = :id")
    Optional<Turn> findByIdWithRound(@Param("id") UUID id);

    @Query("SELECT t FROM Turn t LEFT JOIN t.round LEFT JOIN FETCH t.player WHERE t.round.id = :roundId")
    List<Turn> findByRoundIdWithPlayer(UUID roundId);

    @Query("SELECT t FROM Turn t LEFT JOIN FETCH t.player WHERE t.id = :id")
    Optional<Turn> findByIdWithPlayer(@Param("id") UUID id);

    @Query("SELECT t FROM Turn t LEFT JOIN FETCH t.player WHERE t.player.id = :playerId")
    List<Turn> findAllByPlayerId(UUID playerId);

    @Query("SELECT t FROM Turn t LEFT JOIN t.player LEFT JOIN FETCH t.round WHERE t.round.id = :roundId ORDER BY t.createdAt DESC LIMIT 1")
    Optional<Turn> findLastTurnByRoundIdWithPlayer(UUID roundId);

    @Query("SELECT t FROM Turn t LEFT JOIN FETCH t.player WHERE t.player.id = :playerId AND t.round.id = :roundId")
    List<Turn> findAllByPlayerIdAndRoundId(UUID playerId, UUID roundId);

}
