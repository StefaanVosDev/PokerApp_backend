package be.kdg.poker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @PositiveOrZero
    private int money;
    @ManyToOne
    private Game game;
    @OneToMany
    private List<Turn> turns;
    @ManyToMany
    private List<Card> hand;
    private int position;
    @ManyToOne
    private Account account;
    private String username;

    public Player() {
    }

    public Player(int money) {
        this.money = money;
        this.hand = new ArrayList<>();
    }

    public Player(UUID id, int money) {
        this.money = money;
        this.id = id;
        this.hand = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
