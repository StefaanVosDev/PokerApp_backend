package be.kdg.poker.repositories;

import be.kdg.poker.domain.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, UUID> {
    Optional<Avatar> findAvatarByName(String name);

    @Query("""
    SELECT av
    FROM Account a
    LEFT JOIN a.activeAvatar av
    WHERE a.id = :accountId
    """)
    Optional<Avatar> findByAccountId(UUID accountId);
}
