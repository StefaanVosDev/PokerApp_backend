package be.kdg.poker.repositories;

import be.kdg.poker.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Query("""
    SELECT fr
    FROM FriendRequest fr
    LEFT JOIN FETCH fr.requestingFriend
    WHERE fr.id = :id
    """)
    Optional<FriendRequest> findFriendRequestById(UUID id);

    @Query("""
    SELECT fr
    FROM FriendRequest fr
    LEFT JOIN fr.account a
    WHERE a.username = :accountUsername
    """)
    List<FriendRequest> findFriendRequestByAccountUsername(String accountUsername);

    @Query("""
    SELECT fr
    FROM FriendRequest fr
    LEFT JOIN fr.requestingFriend f
    LEFT JOIN fr.account a
    WHERE f.username = :friendUsername AND a.username = :accountUsername
    """)
    Optional<FriendRequest> findFriendRequestByAccountToFriend(String accountUsername, String friendUsername);

    @Query("""
    SELECT fr
    FROM FriendRequest fr
    LEFT JOIN fr.requestingFriend f
    LEFT JOIN fr.account a
    WHERE f.username = :accountUsername AND a.username = :friendUsername
    """)
    Optional<FriendRequest> findFriendRequestByFriendToAccount(String friendUsername, String accountUsername);

    @Query("""
    SELECT gn
    FROM GameNotification gn
    LEFT JOIN FETCH gn.game
    LEFT JOIN FETCH gn.account a
    WHERE a.username = :accountUsername
    """)
    List<GameNotification> findGameNotificationsByAccountUsername(String accountUsername);
    @Query("""
    SELECT an
    FROM AchievementNotification an
    LEFT JOIN FETCH an.achievement
    LEFT JOIN FETCH an.account a
    WHERE a.username = :accountUsername
    """)
    List<AchievementNotification> findAchievementNotificationsByAccountUsername(String accountUsername);

    @Query("""
    SELECT gin
    FROM InviteNotification gin
    LEFT JOIN FETCH gin.game
    LEFT JOIN FETCH gin.account a
    WHERE a.username = :accountUsername
    """)
    List<InviteNotification> findGameInvitesByAccountUsername(String accountUsername);
}