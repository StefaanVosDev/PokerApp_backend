package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.CardDto;
import be.kdg.poker.controllers.dto.PlayersHandDto;
import be.kdg.poker.domain.Card;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.Round;
import be.kdg.poker.domain.enums.GameStatus;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.PlayersHandNotFoundException;
import be.kdg.poker.exceptions.RoundNotFoundException;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.PlayerRepository;
import be.kdg.poker.repositories.RoundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CardService {
    private final PlayerRepository playerRepository;
    private final HandRankService handRankService;
    private final GameRepository gameRepository;
    private final RoundRepository roundRepository;

    public CardService(PlayerRepository playerRepository, HandRankService handRankService, GameRepository gameRepository, RoundRepository roundRepository) {
        this.playerRepository = playerRepository;
        this.handRankService = handRankService;
        this.gameRepository = gameRepository;
        this.roundRepository = roundRepository;
    }

    public PlayersHandDto getPlayersHand(UUID playerId) {
        log.info("Fetching player with ID: {}", playerId);


        Player player = playerRepository.findByIdWithHand(playerId)
                .orElseThrow(() -> {
                    log.error("Player not found with ID: {}", playerId);
                    return new PlayersHandNotFoundException("Player not found with ID: " + playerId);
                });

        log.info("Player with ID: {} found. Mapping to PlayersHandDto.", playerId);

        Game game = gameRepository.findGameByPlayerId(playerId)
                .orElseThrow(() -> {
                    log.error("Game not found for player with ID: {}", playerId);
                    return new GameNotFoundException("Game not found for player with ID: " + playerId);
                });

        var score = 0;
        if (game.getStatus() == GameStatus.IN_PROGRESS) {
            Round round = roundRepository.findLatestByGameIdWithCommunityCards(game.getId())
                    .orElseThrow(() -> {
                        log.error("Round not found for game with ID: {}", game.getId());
                        return new RoundNotFoundException("Round not found for game with ID: " + game.getId());
                    });
            List<Card> fullHand = new ArrayList<>(round.getCommunityCards());
            fullHand.addAll(player.getHand());

            score = handRankService.calculateHandScore(fullHand);
        }

        // Create PlayersHandDto
        PlayersHandDto playersHandDto = new PlayersHandDto(
                player.getId(),
                player.getGame() != null ? player.getGame().getId() : null,
                player.getHand(),
                score
        );

        log.info("Player's hand successfully mapped to DTO for player ID: {}", playerId);
        return playersHandDto;
    }

    public CardDto mapToDto(Card card) {
        return new CardDto(card.getId(), card.getSuit(), card.getRank());
    }
}
