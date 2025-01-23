package be.kdg.poker.services;

import be.kdg.poker.domain.events.game.GameBetEvent;
import be.kdg.poker.domain.events.game.GameDecisionEvent;
import be.kdg.poker.domain.events.game.GameResultEvent;
import be.kdg.poker.domain.events.game.GameSessionEvent;
import be.kdg.poker.domain.events.user.UserActivityEvent;
import be.kdg.poker.domain.events.user.UserProfileEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // Om objecten naar JSON te converteren

    @Autowired
    public EventService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendUserProfileEvent(UserProfileEvent event) {
        sendEvent("user-profile", event);
    }

    public void sendUserActivityEvent(UserActivityEvent event) {
        sendEvent("user-activity", event);
    }

    public void sendGameSessionEvent(GameSessionEvent event) {
        sendEvent("game-sessions", event);
    }

    public void sendGameBetEvent(GameBetEvent event) {
        sendEvent("game-bets", event);
    }

    public void sendGameDecisionEvent(GameDecisionEvent event) {
        sendEvent("game-decisions", event);
    }

    public void sendGameResultEvent(GameResultEvent event) {
        sendEvent("game-results", event);
    }

    private void sendEvent(String routingKey, Object event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event); // Converteer naar JSON
            rabbitTemplate.convertAndSend("game-metrics-exchange", routingKey, jsonMessage, message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            }); // Stuur naar RabbitMQ
            log.info("Event sent [{}]: {}%n", routingKey, jsonMessage);
        } catch (JsonProcessingException e) {
            log.info("Failed to send event [{}]: {}%n", routingKey, e.getMessage());
        }
    }
}
