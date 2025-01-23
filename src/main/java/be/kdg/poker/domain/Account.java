package be.kdg.poker.domain;


import be.kdg.poker.domain.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Email
    private String email;
    @Size(min = 3, max = 28)
    @Column(unique = true)
    private String username;
    @Size(min = 3)
    private String name;
    @Column(nullable = true)
    private LocalDate age;
    @Size(min = 3)
    private String city;
    private Gender gender;
    @Positive
    private int level = 1;
    @OneToMany(orphanRemoval = true, mappedBy = "account")
    private List<Notification> notifications;
    @ManyToOne
    private Avatar activeAvatar;
    @ManyToMany
    private List<Avatar> avatars;
    @ManyToMany
    private List<Account> friends;
    @OneToMany(orphanRemoval = true)
    private List<Player> players;
    @ManyToMany
    private Set<Achievement> achievements = new HashSet<>();
    private int pokerPoints = 200;
    @ElementCollection
    @CollectionTable(name = "account_counters")
    @MapKeyColumn(name = "counter_name")
    @Column(name = "counter_value")
    private Map<String, Integer> countersAchievements = new HashMap<>();

    public Account() {
        this.countersAchievements.put("wins", 0);
        this.countersAchievements.put("playedGames", 0);
        this.countersAchievements.put("royal flushes", 0);
        this.countersAchievements.put("flushes", 0);
        this.countersAchievements.put("straights", 0);

    }

    public Account(String email, String username, String name, LocalDate age, String city, Gender gender) {
        this.email = email;
        this.username = username;
        this.name = name;
        this.age = age;
        this.city = city;
        this.gender = gender;
        this.notifications = new ArrayList<>();
        this.avatars = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.players = new ArrayList<>();
        this.achievements = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
