package be.kdg.poker.repositories;


import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    @Query("SELECT dm FROM DirectMessage dm WHERE (dm.sender = :sender AND dm.receiver = :receiver) OR (dm.sender = :receiver AND dm.receiver = :sender) ORDER BY dm.timestamp ASC")
    Optional<List<DirectMessage>> findDirectMessagesBySenderAndReceiver(@Param("sender") Account sender, @Param("receiver") Account receiver);

    @Query("SELECT dm FROM DirectMessage dm WHERE dm.sender = :sender AND dm.receiver = :receiver ORDER BY dm.timestamp ASC")
    Optional<List<DirectMessage>> findDirectMessagesBySender(@Param("sender") Account sender, @Param("receiver") Account receiver);

}
