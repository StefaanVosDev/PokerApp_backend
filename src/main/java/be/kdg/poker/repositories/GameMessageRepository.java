package be.kdg.poker.repositories;

import be.kdg.poker.domain.GameMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameMessageRepository extends JpaRepository<GameMessage, UUID> {

    @Query("SELECT gm FROM GameMessage gm LEFT JOIN FETCH gm.sender WHERE gm.game.id = :gameId")
    List<GameMessage> findAllByGameIdWithPlayers(UUID gameId);

    @Query("SELECT gm FROM GameMessage gm WHERE gm.sender.id = :playerId")
    List<GameMessage> findAllByPlayerId(UUID playerId);

}
