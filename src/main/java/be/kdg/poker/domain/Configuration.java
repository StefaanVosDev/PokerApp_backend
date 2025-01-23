package be.kdg.poker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
public class Configuration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Positive
    private int smallBlind;
    @Positive
    private int bigBlind;
    private boolean timer;
    @Positive
    private int startingChips;


    public Configuration() {
    }
}
