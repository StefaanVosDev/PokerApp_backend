package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.RoundDto;
import be.kdg.poker.domain.*;
import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.exceptions.ResourceNotFoundException;
import be.kdg.poker.exceptions.TurnNotFoundException;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.RoundRepository;
import be.kdg.poker.repositories.TurnRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

@Service
@Slf4j
public class RoundService {

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final TurnRepository turnRepository;
    private final TurnService turnService;
    private final GameRoundService gameRoundService;
    private final NotificationService notificationService;

    public RoundService(RoundRepository roundRepository, GameRepository gameRepository, TurnRepository turnRepository, TurnService turnService, GameRoundService gameRoundService, NotificationService notificationService) {
        this.roundRepository = roundRepository;
        this.gameRepository = gameRepository;
        this.turnRepository = turnRepository;
        this.turnService = turnService;
        this.gameRoundService = gameRoundService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void handleTurnAction(UUID turnId, UUID gameId, UUID roundId, Consumer<Turn> action) throws TurnNotFoundException {
        turnService.getByIdWithRound(turnId)
                .ifPresentOrElse(
                        turn -> {
                            var round = findById(roundId);
                            turn.setRound(round);
                            action.accept(turn);
                            changePhase(gameId, roundId);
                            log.info("Successfully executed action for turn with id: {}", turnId);
                        },
                        () -> {
                            throw new TurnNotFoundException("Error executing action for turn with id: " + turnId);
                        }
                );
    }

    public Round findById(UUID roundId) {
        return roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));
    }

    public RoundDto mapToDto(Round round) {
        return new RoundDto(round.getId(), round.getPhase(), round.getDealerIndex(), null, null, null, null);
    }


    public List<Card> getCommunityCards(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        Round round = roundRepository.findByGameWithCommunityCards(game)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));
        return round.getCommunityCards();
    }

    public Optional<Round> getLatestByGameWithTurns(UUID gameId) {
        return roundRepository.findLatestByGameWithTurns(gameId);
    }

    @Transactional
    public void addCommunityCardsBasedOnPhase(Round round) {
        if (round.getPhase() == Phase.FLOP) {
            log.info("Adding 3 community cards to round with id {}", round.getId());
            for (int i = 0; i < 3; i++) {
                addCommunityCard(round);
            }
        } else if (round.getPhase() == Phase.TURN || round.getPhase() == Phase.RIVER) {
            log.info("Adding 1 community card to round with id {}", round.getId());
            addCommunityCard(round);
        }
    }

    @Transactional
    public void addCommunityCard(Round round) {
        List<Card> deck = roundRepository.findDeckByRoundId(round.getId());

        Collections.shuffle(deck);

        if (!deck.isEmpty()) {
            Card card = deck.remove(0);
            round.getCommunityCards().add(card);
            round.setDeck(deck);

            roundRepository.save(round);
        } else {
            throw new IllegalStateException("Deck is empty, cannot add community card");
        }
    }

    @Transactional
    public void changePhase(UUID gameId, UUID roundId) {
        log.info("Changing phase for game with id {} and round with id {}", gameId, roundId);
        Game game = getGameWithPlayers(gameId);
        Round round = getRoundWithTurns(roundId);

        List<Player> playersLeftInRound = getPlayersLeftInRound(round, game, false);
        List<Player> playersAllin = getPlayersInRoundThatWentAllin(round);

        if ((playersLeftInRound.size() == 1 && playersAllin.isEmpty()) || (playersLeftInRound.isEmpty() && playersAllin.size() == 1)) {
            round.setPhase(Phase.FINISHED);
            roundRepository.save(round);
            log.info("round with id {} of game with id {} finished", roundId, gameId);
        } else {
            processRoundPhaseChange(gameId, roundId, game, round, playersLeftInRound, playersAllin);
        }
    }

    @Transactional
    public void processRoundPhaseChange(UUID gameId, UUID roundId, Game game, Round round, List<Player> playersLeftInRound, List<Player> playersAllin) {
        boolean allPlayersMoved = validateAllPlayersHaveMadeMoves(game, round);
        boolean betsMatched = validatePlayersHaveMatchedBets(game, round);

        if (allPlayersMoved && ((playersLeftInRound.size() == 1 && !playersAllin.isEmpty()) || (playersLeftInRound.isEmpty() && playersAllin.size() > 1))) {
            runThroughLastRound(gameId, roundId, round);
        } else if (allPlayersMoved && betsMatched) {
            advanceRoundPhase(game, round);
            log.info("Phase changed for game with id {} and round with id {}", gameId, roundId);
        } else {
            nextPlayerTurn(round.getId(), game.getId());
            log.info("Next player turn for game with id {} and round with id {}", gameId, roundId);
        }
    }

    @Transactional
    public void advanceRoundPhase(Game game, Round round) {
        changeRoundPhase(round);
        addCommunityCardsBasedOnPhase(round);
        roundRepository.save(round);
        nextPlayerTurn(round.getId(), game.getId());
    }

    @Transactional
    public void runThroughLastRound(UUID gameId, UUID roundId, Round round) {
        if (round.getPhase() == Phase.PRE_FLOP) {
            round.setPhase(Phase.FLOP);
            addCommunityCardsBasedOnPhase(round);
        }
        if (round.getPhase() == Phase.FLOP) {
            round.setPhase(Phase.TURN);
            addCommunityCardsBasedOnPhase(round);
        }
        if (round.getPhase() == Phase.TURN) {
            round.setPhase(Phase.RIVER);
            addCommunityCardsBasedOnPhase(round);
        }
        round.setPhase(Phase.FINISHED);
        roundRepository.save(round);
        log.info("All players in game with id {} and round with id {} went allin", gameId, roundId);
    }

    public Game getGameWithPlayers(UUID gameId) {
        return gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
    }

    public Round getRoundWithTurns(UUID roundId) {
        return roundRepository.findByIdWithTurns(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found with id: " + roundId));
    }

    public boolean validatePlayersHaveMatchedBets(Game game, Round round) {
        log.info("Validating if players have matched bets");

        Map<Player, Integer> playerMoneyGambled = new HashMap<>();
        Set<Player> playersThatWentAllin = new HashSet<>();
        int maxMoneyGambled = 0;

        for (Turn turn : round.getTurns()) {
            if (turn.getRound().getPhase() == round.getPhase()) {
                Player player = turn.getPlayer();
                playerMoneyGambled.put(player, playerMoneyGambled.getOrDefault(player, 0) + turn.getMoneyGambled());
                if (turn.getMoveMade() == PlayerStatus.ALL_IN) {
                    playersThatWentAllin.add(turn.getPlayer());
                }
                maxMoneyGambled = Math.max(maxMoneyGambled, turn.getMoneyGambled());
            }
        }

        return processAllPlayersHaveMadeBets(game, round, playerMoneyGambled, playersThatWentAllin, maxMoneyGambled);
    }

    private boolean processAllPlayersHaveMadeBets(Game game, Round round, Map<Player, Integer> playerMoneyGambled, Set<Player> playersThatWentAllin, int maxMoneyGambled) {
        for (Player player : getPlayersLeftInRound(round, game, false)) {
            int totalMoneyThatPlayerGambled = playerMoneyGambled.getOrDefault(player, 0);
            boolean playerWentAllin = playersThatWentAllin.contains(player);

            if (!playerWentAllin && totalMoneyThatPlayerGambled < maxMoneyGambled) {
                log.info("Player with id {} did not match the bet", player);
                return false;
            }
        }

        log.info("All players matched the bet");
        return true;
    }

    public boolean validateAllPlayersHaveMadeMoves(Game game, Round round) {
        log.info("Validating if all players have made moves");
        List<Player> activePlayers = new ArrayList<>(getPlayersLeftInRound(round, game, true));

        Map<Player, Long> mapPlayers = new HashMap<>();
        for (Player player : activePlayers) {
            mapPlayers.put(player, 0L);
        }

        List<Turn> allTurnsFromEarlierPhases = round.getTurns()
                .stream().filter(turn -> turn.getMadeInPhase() != round.getPhase())
                .toList();

        for (Turn turn : allTurnsFromEarlierPhases) {
            if (turn.getMoveMade() == PlayerStatus.ALL_IN || turn.getMoveMade() == PlayerStatus.FOLD) {
                mapPlayers.remove(turn.getPlayer());
            }
        }

        List<Turn> allTurnsThisPhase = getAllTurnsThisPhase(round);
        List<Turn> allActiveTurns = getAllActiveTurns(allTurnsThisPhase);

        Turn lastActiveMoveMade;
        if (!allActiveTurns.isEmpty()) {
            lastActiveMoveMade = allActiveTurns.get(allActiveTurns.size() - 1);

            removingAllTurnsBeforeLastAction(allTurnsThisPhase, lastActiveMoveMade);
        }

        for (Turn turn : allTurnsThisPhase) {
            mapPlayers.put(turn.getPlayer(), mapPlayers.getOrDefault(turn.getPlayer(), 0L) + 1);
        }

        return haveAllPlayersMadeEqualMoves(mapPlayers);
    }

    private boolean haveAllPlayersMadeEqualMoves(Map<Player, Long> mapPlayers) {
        Long movesMade = mapPlayers.values().stream().findAny().orElse(0L);
        for (Long moveCount : mapPlayers.values()) {
            if (!moveCount.equals(movesMade)) {
                return false;
            }
        }
        return true;
    }

    private void removingAllTurnsBeforeLastAction(List<Turn> allTurnsThisPhase, Turn lastActiveMoveMade) {
        Iterator<Turn> iterator = allTurnsThisPhase.iterator();
        while (iterator.hasNext()) {
            Turn turn = iterator.next();
            if (turn.equals(lastActiveMoveMade)) {
                break;
            } else if (turn.getMoveMade() != PlayerStatus.ALL_IN) {
                iterator.remove();
            }
        }
    }

    private List<Turn> getAllActiveTurns(List<Turn> allTurnsThisPhase) {
        return allTurnsThisPhase.stream()
                .filter(turn -> turn.getMoveMade() == PlayerStatus.RAISE || (turn.getMoveMade() == PlayerStatus.ALL_IN &&
                        turn.getMoneyGambled() >= allTurnsThisPhase.stream().map(Turn::getMoneyGambled).max(Integer::compareTo).orElse(0)))
                .toList();
    }

    private ArrayList<Turn> getAllTurnsThisPhase(Round round) {
        return new ArrayList<>(round.getTurns().stream()
                .filter(turn -> turn.getMadeInPhase() == round.getPhase() && turn.getMoveMade() != PlayerStatus.FOLD && turn.getMoveMade() != PlayerStatus.SMALL_BLIND && turn.getMoveMade() != PlayerStatus.BIG_BLIND)
                .toList());
    }

    @Transactional
    public void changeRoundPhase(Round round) {
        log.info("Changing round phase");
        switch (round.getPhase()) {
            case PRE_FLOP -> round.setPhase(Phase.FLOP);
            case FLOP -> round.setPhase(Phase.TURN);
            case TURN -> round.setPhase(Phase.RIVER);
            default -> round.setPhase(Phase.FINISHED);
        }
        roundRepository.save(round);
    }

    @Transactional
    public void startRound(UUID gameId, UUID roundId) {
        log.info("Starting round for game with id {} and round with id {}", gameId, roundId);
        Game game = getGameWithPlayers(gameId);
        Round round = getRoundWithTurns(roundId);

        turnService.addSmallAndBigBlindsToRound(round, game);

        TurnService.addFirstPlayer(round, game, turnRepository, roundRepository);
    }

    public Optional<Round> getCurrentRound(UUID gameId) {
        return roundRepository.findLatestByGameWithDeck(gameId);
    }

    @Transactional
    public void nextPlayerTurn(UUID roundId, UUID gameId) {
        log.info("Getting next player turn for round with id {} and game with id {}", roundId, gameId);
        Round round = getRoundWithTurns(roundId);
        Game game = getGameWithPlayers(gameId);

        List<Player> playersLeftInRound = getPlayersLeftInRound(round, game, false);
        List<Player> playersFoldedOrWentAllinThisPhase = getPlayersFoldedOrWentAllinThisPhase(round);

        playersLeftInRound.addAll(playersFoldedOrWentAllinThisPhase);
        playersLeftInRound.sort(Comparator.comparing(Player::getPosition));

        List<Turn> turnsThisPhase = getTurnsThisPhase(round);
        Player nextPlayer = new Player();
        if (!turnsThisPhase.isEmpty()) {
            nextPlayer = findNextPlayerAfterPreviousMovesInPhase(turnsThisPhase, playersLeftInRound, nextPlayer, playersFoldedOrWentAllinThisPhase);
        } else {
            nextPlayer = findNextPlayerAtBeginningOfPhase(round, game, playersLeftInRound, nextPlayer);
        }

        Turn nextTurn = new Turn(nextPlayer, PlayerStatus.ON_MOVE, round, round.getPhase());
        notificationService.notifyPlayerOnMove(nextPlayer, game);

        turnRepository.save(nextTurn);
        round.getTurns().add(nextTurn);
        roundRepository.save(round);

        gameRoundService.isLoggedInUserOnMove(gameId);
    }

    private List<Turn> getTurnsThisPhase(Round round) {
        return round.getTurns().stream()
                .filter(turn -> turn.getMadeInPhase() == round.getPhase())
                .toList();
    }

    private List<Player> getPlayersFoldedOrWentAllinThisPhase(Round round) {
        return round.getTurns().stream()
                .filter(turn -> (turn.getMoveMade() == PlayerStatus.FOLD || turn.getMoveMade() == PlayerStatus.ALL_IN) && turn.getMadeInPhase() == round.getPhase())
                .map(Turn::getPlayer)
                .toList();
    }

    private Player findNextPlayerAtBeginningOfPhase(Round round, Game game, List<Player> playersLeftInRound, Player nextPlayer) {
        int index = round.getDealerIndex();
        boolean nextPlayerFound = false;
        while (!nextPlayerFound) {
            List<Player> players = new ArrayList<>(game.getPlayers());
            players.sort(Comparator.comparing(Player::getPosition));
            Player nextPlayerInGame = players.get((index + 1) % players.size());
            if (playersLeftInRound.contains(nextPlayerInGame)) {
                nextPlayer = nextPlayerInGame;
                nextPlayerFound = true;
            }
            index++;
        }
        return nextPlayer;
    }

    private Player findNextPlayerAfterPreviousMovesInPhase(List<Turn> turnsThisPhase, List<Player> playersLeftInRound, Player nextPlayer, List<Player> playersFoldedOrWentAllinThisPhase) {
        Turn lastTurn = turnsThisPhase.get(turnsThisPhase.size() - 1);
        Player currentPlayer = lastTurn.getPlayer();

        int currentPlayerIndex = playersLeftInRound.indexOf(currentPlayer);
        int nextPlayerIndex = (currentPlayerIndex + 1) % (playersLeftInRound.size());
        boolean nextPlayerFound = false;
        while (!nextPlayerFound) {
            nextPlayer = playersLeftInRound.get(nextPlayerIndex);
            if (playersFoldedOrWentAllinThisPhase.contains(nextPlayer)) {
                nextPlayerIndex = (nextPlayerIndex + 1) % (playersLeftInRound.size());
            } else {
                nextPlayer = playersLeftInRound.get(nextPlayerIndex);
                nextPlayerFound = true;
            }
        }
        return nextPlayer;
    }

    @Transactional
    public Round createNewRoundIfFinished(Game game, UUID roundId) {
        log.info("Creating new round if finished for game with id {} and round with id {}", game, roundId);
        Round round = getRoundWithTurns(roundId);

        if (round.getPhase() == Phase.FINISHED) {
            Round newRound = gameRoundService.create(game);
            newRound.setDealerIndex((round.getDealerIndex() + 1) % game.getPlayers().size());
            gameRoundService.assignPlayerHand(game.getId(), newRound);
            turnService.addSmallAndBigBlindsToRound(newRound, game);
            turnService.create(newRound, game);
            return newRound;
        }
        return null;
    }

    public List<Player> getPlayersLeftInRound(Round round, Game game, boolean includeAllin) {
        log.info("Getting players left in round");
        List<Player> activePlayers = new ArrayList<>(game.getPlayers());
        List<Turn> earlierTurns = round.getTurns();

        for (Turn turn : earlierTurns) {
            if (turn.getMoveMade() == PlayerStatus.FOLD || (!includeAllin && turn.getMoveMade() == PlayerStatus.ALL_IN)) {
                activePlayers.remove(turn.getPlayer());
            }
        }
        return activePlayers;
    }

    public List<Player> getPlayersInRoundThatWentAllin(Round round) {
        return round.getTurns().stream()
                .filter(turn -> turn.getMoveMade() == PlayerStatus.ALL_IN)
                .map(Turn::getPlayer)
                .toList();
    }
}