package be.kdg.poker.repositories;

import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findAccountByEmailAndUsername(String email, String username);

    Optional<Account> findAccountByEmail(String email);

    Optional<Account> findAccountByUsername(String username);

    @Query("""
        SELECT a
        FROM Account a
        LEFT JOIN FETCH a.avatars
        WHERE a.username = :username
        """)
    Optional<Account> findByUsernameWithAvatars(String username);

    @Query("""
           SELECT a
           FROM Account a
           LEFT JOIN FETCH a.countersAchievements
           WHERE a.id = :accountId
           """)
    Optional<Account> findByAccountIdWithCounters(UUID accountId);

    @Query("""
            SELECT a
            FROM Account a
            LEFT JOIN FETCH a.avatars
            WHERE a.email = :email
            """)
    Optional<Account> findAccountByEmailWithAvatars(String email);

    @Query("""
    SELECT a
    FROM Account a
    LEFT JOIN FETCH a.friends
    WHERE a.username = :username
    """)
    Optional<Account> findAccountByUsernameWithFriends(String username);

    @Query("""
    SELECT a
    FROM Account a
    LEFT JOIN a.notifications n
    LEFT JOIN FETCH a.friends
    WHERE n.id = :notificationId
    """)
    Optional<Account> findByNotificationIdWithFriends(UUID notificationId);

    @Query("""
    SELECT fr
    FROM Account a
    LEFT JOIN a.friends fr
    WHERE a.id = :id
    """)
    List<Account> findFriendsById(UUID id);

    @Query("SELECT a.achievements FROM Account a WHERE a.id = :accountId")
    List<Achievement> findAllAchievementsByAccountId(UUID accountId);

}
