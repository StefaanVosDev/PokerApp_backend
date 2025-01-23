package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.TurnDto;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.Round;
import be.kdg.poker.domain.Turn;
import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.domain.events.game.GameBetEvent;
import be.kdg.poker.domain.events.game.GameDecisionEvent;
import be.kdg.poker.exceptions.RoundNotFoundException;
import be.kdg.poker.repositories.PlayerRepository;
import be.kdg.poker.repositories.RoundRepository;
import be.kdg.poker.repositories.TurnRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class TurnService {
    private final TurnRepository turnRepository;
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;
    private final RoundRepository roundRepository;
    private final EventService eventService;

    public TurnService(TurnRepository turnRepository, PlayerRepository playerRepository, PlayerService playerService, RoundRepository roundRepository, EventService eventService) {
        this.turnRepository = turnRepository;
        this.playerRepository = playerRepository;
        this.playerService = playerService;
        this.roundRepository = roundRepository;
        this.eventService = eventService;
    }

    @Transactional
    public Turn create(Round round, Game game) {
        var turnsPassed = round.getTurns().size();
        var players = playerRepository.findAllByGameId(game.getId());
        players.sort(Comparator.comparing(Player::getPosition));
        game.setPlayers(players);
        var playerIndex = (round.getDealerIndex() + turnsPassed + 1) % players.size();
        var currentPlayer = players.get(playerIndex);
        Turn newTurn = new Turn(currentPlayer, PlayerStatus.ON_MOVE, round, round.getPhase());

        List<Turn> roundTurns = round.getTurns();
        roundTurns.add(newTurn);
        round.setTurns(roundTurns);
        return turnRepository.save(newTurn);
    }


    public Optional<Turn> getById(UUID id) {
        return turnRepository.findById(id);
    }

    public Optional<Turn> getByIdWithRound(UUID id) {
        return turnRepository.findByIdWithRound(id);
    }

    @Transactional
    public Turn check(Turn turn) {
        turn.setMoveMade(PlayerStatus.CHECK);
        turnRepository.save(turn);

        sendGameDecisionEvent(turn, "check", calculateDecisionSpeed(turn));
        return turn;
    }

    @Transactional
    public void fold(Turn turn) {
        turn.setMoveMade(PlayerStatus.FOLD);
        turnRepository.save(turn);

        sendGameDecisionEvent(turn, "fold", calculateDecisionSpeed(turn));
    }

    @Transactional
    public void call(Turn turn, int amount) {
        turn.setMoveMade(PlayerStatus.CALL);
        turn.setMoneyGambled(amount);
        turnRepository.save(turn);

        sendGameBetEvent(turn, amount);
        sendGameDecisionEvent(turn, "call", calculateDecisionSpeed(turn));
    }


    @Transactional
    public void raise(Turn turn, int amount) {
        turn.setMoveMade(PlayerStatus.RAISE);
        turn.setMoneyGambled(amount);
        turnRepository.save(turn);

        sendGameBetEvent(turn, amount);
        sendGameDecisionEvent(turn, "raise", calculateDecisionSpeed(turn));
    }

    @Transactional
    public void allin(Turn turn, int amount) {
        turn.setMoveMade(PlayerStatus.ALL_IN);
        turn.setMoneyGambled(amount);
        turnRepository.save(turn);

        sendGameBetEvent(turn, amount);
        sendGameDecisionEvent(turn, "allin", calculateDecisionSpeed(turn));
    }

    public List<Turn> getAllByRoundIdWithPlayer(UUID roundId) {
        return turnRepository.findByRoundIdWithPlayer(roundId);
    }

    @Transactional
    public void addSmallAndBigBlindsToRound(Round round, Game game) {
        var players = new ArrayList<>(game.getPlayers());
        var settings = game.getSettings();
        players.sort(Comparator.comparing(Player::getPosition));

        Player smallBlindPlayer = players.get((round.getDealerIndex() + 1) % players.size());
        Turn smallBlindTurn;

        if (smallBlindPlayer.getMoney() < settings.getSmallBlind()) {
            smallBlindTurn = new Turn(smallBlindPlayer, PlayerStatus.ALL_IN, round, Phase.PRE_FLOP);
            smallBlindTurn.setMoneyGambled(smallBlindPlayer.getMoney());
            smallBlindPlayer.setMoney(0);
        } else {
            smallBlindTurn = new Turn(smallBlindPlayer, PlayerStatus.SMALL_BLIND, round, Phase.PRE_FLOP);
            smallBlindTurn.setMoneyGambled(settings.getSmallBlind());
            smallBlindPlayer.setMoney(smallBlindPlayer.getMoney() - settings.getSmallBlind());
        }

        Player bigBlindPlayer = players.get((round.getDealerIndex() + 2) % players.size());
        Turn bigBlindTurn;

        if (bigBlindPlayer.getMoney() < settings.getBigBlind()) {
            bigBlindTurn = new Turn(bigBlindPlayer, PlayerStatus.ALL_IN, round, Phase.PRE_FLOP);
            bigBlindTurn.setMoneyGambled(bigBlindPlayer.getMoney());
            bigBlindPlayer.setMoney(0);
        } else {
            bigBlindTurn = new Turn(bigBlindPlayer, PlayerStatus.BIG_BLIND, round, Phase.PRE_FLOP);
            bigBlindTurn.setMoneyGambled(settings.getBigBlind());
            bigBlindPlayer.setMoney(bigBlindPlayer.getMoney() - settings.getBigBlind());
        }

        turnRepository.save(smallBlindTurn);
        turnRepository.save(bigBlindTurn);

        playerRepository.saveAll(List.of(smallBlindPlayer, bigBlindPlayer));

        round.getTurns().addAll(List.of(smallBlindTurn, bigBlindTurn));
    }

    @Transactional
    public void createAndSaveInitialTurns(Round round, Game game) {
        addSmallAndBigBlindsToRound(round, game);
        addFirstPlayer(round, game, turnRepository, roundRepository);
    }

    static void addFirstPlayer(Round round, Game game, TurnRepository turnRepository, RoundRepository roundRepository) {
        List<Player> players = new ArrayList<>(game.getPlayers());
        players.sort(Comparator.comparing(Player::getPosition));
        Player firstPlayer = players.get((round.getDealerIndex() + 3) % players.size());
        Turn turn = new Turn(firstPlayer, PlayerStatus.ON_MOVE, round, Phase.PRE_FLOP);
        turnRepository.save(turn);
        round.getTurns().add(turn);
        roundRepository.save(round);
    }

    public TurnDto mapToDtoWithPlayerAndRound(Turn turn) {
        return new TurnDto(turn.getId(), turn.getMoveMade(), turn.getMoneyGambled(), turn.getPlayer() == null? null: playerService.mapToDto(turn.getPlayer()), turn.getRound().getId(), turn.getMadeInPhase(), turn.getCreatedAt());
    }

    public TurnDto mapToDtoWithPlayer(Turn turn) {
        return new TurnDto(turn.getId(), turn.getMoveMade(), turn.getMoneyGambled(), turn.getPlayer() == null? null : playerService.mapToDto(turn.getPlayer()), null, turn.getMadeInPhase(), turn.getCreatedAt());
    }

    public boolean checkIfAnyInCurrentPhase(List<Turn> turns, UUID roundId) {
        var round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RoundNotFoundException("unable to find round with id " + roundId));
        return turns.stream().anyMatch(turn -> turn.getMadeInPhase().equals(round.getPhase()));
    }

    private void sendGameDecisionEvent(Turn turn, String decision, int decisionSpeed) {
        GameDecisionEvent event = new GameDecisionEvent(
                turn.getPlayer().getAccount().getId().toString(),
                turn.getRound().getGame().getId().toString(),
                decision,
                decisionSpeed,
                LocalDateTime.now()
        );
        eventService.sendGameDecisionEvent(event);
    }
    private int calculateDecisionSpeed(Turn turn) {
        Turn previousTurn = turnRepository.findLastTurnByRoundIdWithPlayer(turn.getRound().getId())
                .orElse(null);
        if (previousTurn == null) return 0;

        return (int) java.time.Duration.between(previousTurn.getCreatedAt(), LocalDateTime.now()).toMillis();
    }
    private void sendGameBetEvent(Turn turn, int amount) {
        GameBetEvent betEvent = new GameBetEvent(
                turn.getPlayer().getAccount().getId().toString(),
                turn.getRound().getGame().getId().toString(),
                amount,
                LocalDateTime.now()
        );
        eventService.sendGameBetEvent(betEvent);
    }

    public Optional<Integer> calculateTimeRemaining(UUID id) {
        return turnRepository.findById(id).map(turn -> {
            var timerExpires = turn.getCreatedAt().plusSeconds(60);
            return (int) Duration.between(LocalDateTime.now(), timerExpires).getSeconds();
                });
    }
}
