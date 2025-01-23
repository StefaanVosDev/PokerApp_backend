package be.kdg.poker.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String message;
    private LocalDateTime timestamp;
    @ManyToOne
    private Account account;


    public Notification() {
    }


    public Notification(String message, Account account) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.account = account;
    }
}
