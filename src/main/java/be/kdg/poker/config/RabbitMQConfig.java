package be.kdg.poker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("game-metrics-exchange");
    }

    @Bean
    public Queue userProfileQueue() {
        // Voor demografische gegevens en profielinformatie van spelers.
        return new Queue("user-profile", true);
    }

    @Bean
    public Queue userActivityQueue() {
        // Voor dagelijkse en maandelijkse gebruikersactiviteiten (bijvoorbeeld login-events, engagement).
        return new Queue("user-activity", true);
    }

    @Bean
    public Queue gameSessionsQueue() {
        // Voor gegevens over spelsessies (bijvoorbeeld sessieduur, start- en eindtijd).
        return new Queue("game-sessions", true);
    }

    @Bean
    public Queue gameBetsQueue() {
        // Voor inzetten en financiële gegevens binnen spellen.
        return new Queue("game-bets", true);
    }

    @Bean
    public Queue gameDecisionsQueue() {
        // Voor acties zoals fold, call, raise, en het meten van beslissingssnelheden.
        return new Queue("game-decisions", true);
    }

    @Bean
    public Queue gameResultsQueue() {
        // Voor winpercentages, trends en uitkomsten van spellen.
        return new Queue("game-results", true);
    }

    @Bean
    public Binding userProfileBinding(Queue userProfileQueue, DirectExchange exchange) {
        return BindingBuilder.bind(userProfileQueue).to(exchange).with("user-profile");
    }

    @Bean
    public Binding userActivityBinding(Queue userActivityQueue, DirectExchange exchange) {
        return BindingBuilder.bind(userActivityQueue).to(exchange).with("user-activity");
    }

    @Bean
    public Binding gameSessionsBinding(Queue gameSessionsQueue, DirectExchange exchange) {
        return BindingBuilder.bind(gameSessionsQueue).to(exchange).with("game-sessions");
    }

    @Bean
    public Binding gameBetsBinding(Queue gameBetsQueue, DirectExchange exchange) {
        return BindingBuilder.bind(gameBetsQueue).to(exchange).with("game-bets");
    }

    @Bean
    public Binding gameDecisionsBinding(Queue gameDecisionsQueue, DirectExchange exchange) {
        return BindingBuilder.bind(gameDecisionsQueue).to(exchange).with("game-decisions");
    }

    @Bean
    public Binding gameResultsBinding(Queue gameResultsQueue, DirectExchange exchange) {
        return BindingBuilder.bind(gameResultsQueue).to(exchange).with("game-results");
    }
}

