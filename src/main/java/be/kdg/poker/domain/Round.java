package be.kdg.poker.domain;

import be.kdg.poker.domain.enums.Phase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Phase phase;
    @Min(0)
    @Max(5)
    private int dealerIndex;
    @OneToMany
    private List<Turn> turns;
    @ManyToMany
    private List<Card> communityCards;
    @ManyToMany
    private List<Card> deck;
    @ManyToOne
    private Game game;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Round() {
    }

    public Round(Phase phase, List<Card> deck, Game game) {
        this.phase = phase;
        this.deck = deck;
        this.communityCards = new ArrayList<>();
        this.turns = new ArrayList<>();
        this.game = game;
        this.createdAt = LocalDateTime.now();
    }
}
