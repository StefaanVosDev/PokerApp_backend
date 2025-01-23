package be.kdg.poker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AchievementNotification extends Notification {
    @ManyToOne
    private Achievement achievement;

    public AchievementNotification(Achievement achievement, String message, Account account) {
        super(message, account);
        this.achievement = achievement;
    }

    public AchievementNotification() {

    }
}
