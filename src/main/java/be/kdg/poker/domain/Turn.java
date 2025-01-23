package be.kdg.poker.domain;

import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.enums.PlayerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Turn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    private Player player;
    private PlayerStatus moveMade;
    @PositiveOrZero
    private int moneyGambled;
    @ManyToOne
    private Round round;
    private Phase madeInPhase;
    private LocalDateTime createdAt;

    public Turn() {
        createdAt = LocalDateTime.now();
    }

    public Turn(Player player) {
        this.player = player;
        this.createdAt = LocalDateTime.now();
    }

    public Turn(Player player, PlayerStatus moveMade, Round round, Phase madeInPhase) {
        this.player = player;
        this.moveMade = moveMade;
        this.round = round;
        this.madeInPhase = madeInPhase;
        this.createdAt = LocalDateTime.now();
    }
}
