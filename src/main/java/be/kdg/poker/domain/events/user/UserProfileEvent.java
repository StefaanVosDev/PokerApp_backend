package be.kdg.poker.domain.events.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserProfileEvent {
    private String userId;
    private int age;
    private String city;
    private String gender;
    private LocalDateTime registrationDate;

    // Constructor
    public UserProfileEvent(String userId, int age, String city, String gender, LocalDateTime registrationDate) {
        this.userId = userId;
        this.age = age;
        this.city = city;
        this.gender = gender;
        this.registrationDate = registrationDate;
    }

    // Default constructor (required for serialization/deserialization)
    public UserProfileEvent() {
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "UserDemographicsEvent{" +
                "userId='" + userId + '\'' +
                ", age=" + age +
                ", city='" + city + '\'' +
                ", gender='" + gender + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
}
