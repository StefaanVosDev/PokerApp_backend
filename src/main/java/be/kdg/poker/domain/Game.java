package be.kdg.poker.domain;

import be.kdg.poker.domain.enums.GameStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private GameStatus status;
    @Min(2)
    @Max(6)
    private int maxPlayers;
    @OneToMany(mappedBy = "game")
    private List<Round> rounds;
    @OneToMany(mappedBy = "game")
    private List<Player> players;
    @OneToOne
    private Player winner;
    @Size(max = 20)
    private String name;
    @OneToOne
    private Configuration settings;
    @OneToMany
    private List<GameMessage> messages;

    public Game() {
    }
}
