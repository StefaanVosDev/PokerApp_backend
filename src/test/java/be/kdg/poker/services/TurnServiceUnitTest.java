package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.PlayerDto;
import be.kdg.poker.controllers.dto.TurnDto;
import be.kdg.poker.domain.*;
import be.kdg.poker.domain.enums.Gender;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.repositories.PlayerRepository;
import be.kdg.poker.repositories.TurnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TurnServiceUnitTest {

    @Autowired
    private TurnService turnService;
    @MockBean
    private TurnRepository turnRepository;
    @MockBean
    private PlayerRepository playerRepository;
    @MockBean
    private PlayerService playerService;


    private Game mockGame;
    private Round mockRound;
    private Turn mockTurn;
    private UUID turnId;
    private UUID roundId;
    private List<Turn> mockTurns;

    @BeforeEach
    void setUp() {
        mockGame = new Game();
        mockRound = new Round();
        mockTurn = new Turn();
        Turn mockTurn2 = new Turn();

        UUID gameId = UUID.randomUUID();
        roundId = UUID.randomUUID();
        turnId = UUID.randomUUID();
        UUID turnId2 = UUID.randomUUID();

        mockGame.setId(gameId);
        mockRound.setId(roundId);
        mockTurn.setId(turnId);
        mockTurn2.setId(turnId2);

        mockTurns = new ArrayList<>();
        mockTurns.addAll(List.of(mockTurn, mockTurn2));

        mockRound.setTurns(mockTurns);
    }

    @Test
    void create_ShouldReturnTurn_WhenSaveIsSuccessful() {
        //Arrange
        var mockPlayer1 = new Player();
        mockPlayer1.setId(UUID.randomUUID());
        var mockPlayer2 = new Player();
        mockPlayer2.setId(UUID.randomUUID());
        var mockPlayers = new ArrayList<>(List.of(mockPlayer1, mockPlayer2));

        mockGame.setPlayers(mockPlayers);
        mockTurn.setPlayer(mockPlayer1);
        when(turnRepository.save(any(Turn.class))).thenReturn(mockTurn);
        when(playerRepository.findAllByGameId(any(UUID.class))).thenReturn(mockPlayers);

        //Act
        var result = turnService.create(mockRound, mockGame);

        //Assert
        assertEquals(mockTurn, result);
        assertEquals(result.getPlayer(), mockPlayer1);
        verify(turnRepository, times(1)).save(any(Turn.class));
    }

    @Test
     void findById_ShouldReturnOptionalTurn_GivenValidId() {
        //Arrange
        when(turnRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Turn()));

        //Act
        var result = turnService.getById(UUID.randomUUID()); //not actually random but existing

        //Assert
        assertTrue(result.isPresent());
        verify(turnRepository, times(1)).findById(any(UUID.class));
    }

    @Test
     void findById_ShouldReturnEmptyOptional_GivenInvalidId() {
        //Arrange
        when(turnRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        //Act
        var result = turnService.getById(UUID.randomUUID());

        //Assert
        assertTrue(result.isEmpty());
    }

    @Test
     void check_ShouldUpdateMoveMade() {
        //Arrange
        mockTurn.setMoveMade(PlayerStatus.CHECK);
        mockRound.setGame(mockGame);
        mockTurn.setRound(mockRound);
        var mockPlayer = new Player();
        mockPlayer.setId(UUID.randomUUID());
        var mockAccount = new Account();
        mockAccount.setCity("MockTown");
        mockAccount.setName("mock account");
        mockAccount.setGender(Gender.MALE);
        mockAccount.setEmail("mock.account@gmail.com");
        mockAccount.setAge(LocalDate.now());
        mockAccount.setUsername("Mockyboi");
        mockAccount.setId(UUID.randomUUID());
        mockAccount.setPokerPoints(200);
        mockAccount.setLevel(1);
        mockPlayer.setAccount(mockAccount);
        mockTurn.setPlayer(mockPlayer);

        when(turnRepository.save(mockTurn)).thenReturn(mockTurn);

        //Act
        var result = turnService.check(mockTurn);

        //Assert
        assertEquals(result, mockTurn);
        assertEquals(PlayerStatus.CHECK, result.getMoveMade());
    }

    @Test
    void fold_ShouldSetMoveMadeToFold_AndSaveTurn() {
        // Arrange
        mockTurn.setMoveMade(PlayerStatus.ON_MOVE);
        mockRound.setGame(mockGame);
        mockTurn.setRound(mockRound);
        var mockPlayer = new Player();
        mockPlayer.setId(UUID.randomUUID());
        var mockAccount = new Account();
        mockAccount.setCity("MockTown");
        mockAccount.setName("mock account");
        mockAccount.setGender(Gender.MALE);
        mockAccount.setEmail("mock.account@gmail.com");
        mockAccount.setAge(LocalDate.now());
        mockAccount.setUsername("Mockyboi");
        mockAccount.setId(UUID.randomUUID());
        mockAccount.setPokerPoints(200);
        mockAccount.setLevel(1);
        mockPlayer.setAccount(mockAccount);
        mockTurn.setPlayer(mockPlayer);

        // Act
        turnService.fold(mockTurn);

        // Assert
        assertEquals(PlayerStatus.FOLD, mockTurn.getMoveMade());
        verify(turnRepository, times(1)).save(mockTurn);
    }

    @Test
    void call_ShouldSetMoveMadeToCall_AndSetMoneyGambled_AndSaveTurn() {
        // Arrange
        mockTurn.setMoveMade(PlayerStatus.ON_MOVE);
        mockRound.setGame(mockGame);
        mockTurn.setRound(mockRound);
        var mockPlayer = new Player();
        mockPlayer.setId(UUID.randomUUID());
        var mockAccount = new Account();
        mockAccount.setCity("MockTown");
        mockAccount.setName("mock account");
        mockAccount.setGender(Gender.MALE);
        mockAccount.setEmail("mock.account@gmail.com");
        mockAccount.setAge(LocalDate.now());
        mockAccount.setUsername("Mockyboi");
        mockAccount.setId(UUID.randomUUID());
        mockAccount.setPokerPoints(200);
        mockAccount.setLevel(1);
        mockPlayer.setAccount(mockAccount);
        mockTurn.setPlayer(mockPlayer);

        // Act
        turnService.call(mockTurn, 10);

        // Assert
        assertEquals(PlayerStatus.CALL, mockTurn.getMoveMade());
        assertEquals(10, mockTurn.getMoneyGambled());
        verify(turnRepository, times(1)).save(mockTurn);
    }

    @Test
    void raise_ShouldSetMoveMadeToRaise_AndSetMoneyGambled_AndSaveTurn() {
        // Arrange
        mockTurn.setMoveMade(PlayerStatus.ON_MOVE);
        mockRound.setGame(mockGame);
        mockTurn.setRound(mockRound);
        var mockPlayer = new Player();
        mockPlayer.setId(UUID.randomUUID());
        var mockAccount = new Account();
        mockAccount.setCity("MockTown");
        mockAccount.setName("mock account");
        mockAccount.setGender(Gender.MALE);
        mockAccount.setEmail("mock.account@gmail.com");
        mockAccount.setAge(LocalDate.now());
        mockAccount.setUsername("Mockyboi");
        mockAccount.setId(UUID.randomUUID());
        mockAccount.setPokerPoints(200);
        mockAccount.setLevel(1);
        mockPlayer.setAccount(mockAccount);
        mockTurn.setPlayer(mockPlayer);

        // Act
        turnService.raise(mockTurn, 10);

        // Assert
        assertEquals(PlayerStatus.RAISE, mockTurn.getMoveMade());
        assertEquals(10, mockTurn.getMoneyGambled());
        verify(turnRepository, times(1)).save(mockTurn);
    }

    @Test
    void allin_ShouldSetMoveMadeToAllin_AndSetMoneyGambled_AndSaveTurn() {
        // Arrange
        mockTurn.setMoveMade(PlayerStatus.ON_MOVE);
        mockRound.setGame(mockGame);
        mockTurn.setRound(mockRound);
        var mockPlayer = new Player();
        mockPlayer.setId(UUID.randomUUID());
        var mockAccount = new Account();
        mockAccount.setCity("MockTown");
        mockAccount.setName("mock account");
        mockAccount.setGender(Gender.MALE);
        mockAccount.setEmail("mock.account@gmail.com");
        mockAccount.setAge(LocalDate.now());
        mockAccount.setUsername("Mockyboi");
        mockAccount.setId(UUID.randomUUID());
        mockAccount.setPokerPoints(200);
        mockAccount.setLevel(1);
        mockPlayer.setAccount(mockAccount);
        mockTurn.setPlayer(mockPlayer);

        // Act
        turnService.allin(mockTurn, 10);

        // Assert
        assertEquals(PlayerStatus.ALL_IN, mockTurn.getMoveMade());
        assertEquals(10, mockTurn.getMoneyGambled());
        verify(turnRepository, times(1)).save(mockTurn);
    }



    @Test
    void getAllByRoundIdWithPlayer_ShouldReturnListOfTurns() {
        // Arrange
        when(turnRepository.findByRoundIdWithPlayer(roundId)).thenReturn(mockTurns);

        // Act
        List<Turn> result = turnService.getAllByRoundIdWithPlayer(roundId);

        // Assert
        assertEquals(mockTurns, result);
    }

    @Test
    void addSmallAndBigBlindsToRound_ShouldAddBlindsAndSaveTurnsAndPlayers() {
        // Arrange
        Player dealerPlayer = new Player();
        Player smallBlindPlayer = new Player();
        smallBlindPlayer.setMoney(100);
        Player bigBlindPlayer = new Player();
        bigBlindPlayer.setMoney(100);
        Configuration mockSettings = new Configuration();
        mockSettings.setBigBlind(25);
        mockSettings.setSmallBlind(15);
        mockSettings.setId(UUID.randomUUID());
        mockGame.setPlayers(List.of(dealerPlayer, smallBlindPlayer, bigBlindPlayer));
        mockGame.setSettings(mockSettings);
        mockRound = new Round();
        mockRound.setTurns(new ArrayList<>());

        // Act
        turnService.addSmallAndBigBlindsToRound(mockRound, mockGame);

        // Assert
        assertEquals(2, mockRound.getTurns().size());

        Turn smallBlindTurn = mockRound.getTurns().get(0);
        assertEquals(PlayerStatus.SMALL_BLIND, smallBlindTurn.getMoveMade());
        assertEquals(15, smallBlindTurn.getMoneyGambled());
        assertEquals(smallBlindPlayer, smallBlindTurn.getPlayer());

        Turn bigBlindTurn = mockRound.getTurns().get(1);
        assertEquals(PlayerStatus.BIG_BLIND, bigBlindTurn.getMoveMade());
        assertEquals(25, bigBlindTurn.getMoneyGambled());
        assertEquals(bigBlindPlayer, bigBlindTurn.getPlayer());

        assertEquals(85, smallBlindPlayer.getMoney());
        assertEquals(75, bigBlindPlayer.getMoney());

        verify(turnRepository, times(1)).save(smallBlindTurn);
        verify(turnRepository, times(1)).save(bigBlindTurn);

        verify(playerRepository, times(1)).saveAll(List.of(smallBlindPlayer, bigBlindPlayer));
    }

    @Test
    void getByIdWithRound_ShouldReturnTurn_WhenTurnExists() {
        // Arrange
        when(turnRepository.findByIdWithRound(turnId)).thenReturn(Optional.of(mockTurn));

        // Act
        Optional<Turn> result = turnService.getByIdWithRound(turnId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockTurn, result.get());
        verify(turnRepository, times(1)).findByIdWithRound(turnId);
    }

    @Test
    void getByIdWithRound_ShouldReturnEmpty_WhenTurnDoesNotExist() {
        // Arrange
        when(turnRepository.findByIdWithRound(turnId)).thenReturn(Optional.empty());

        // Act
        Optional<Turn> result = turnService.getByIdWithRound(turnId);

        // Assert
        assertFalse(result.isPresent());
        verify(turnRepository, times(1)).findByIdWithRound(turnId);
    }

    @Test
    void mapToDtoWithPlayerAndRound_ShouldReturnTurnDto_WithPlayerAndRound() {
        // Act
        mockTurn.setPlayer(new Player());
        mockTurn.setRound(new Round());

        when(playerService.mapToDto(any(Player.class))).thenReturn(new PlayerDto(mockTurn.getPlayer().getId(), mockTurn.getPlayer().getMoney(), mockTurn.getPlayer().getUsername(), mockTurn.getPlayer().getPosition()));

        TurnDto result = turnService.mapToDtoWithPlayerAndRound(mockTurn);

        // Assert
        assertEquals(mockTurn.getId(), result.id());
        assertEquals(mockTurn.getMoveMade(), result.moveMade());
        assertEquals(mockTurn.getMoneyGambled(), result.moneyGambled());
        assertEquals(mockTurn.getPlayer().getId(), result.player().id());
        assertEquals(mockTurn.getRound().getId(), result.roundId());
        assertEquals(mockTurn.getMadeInPhase(), result.madeInPhase());
    }

    @Test
    void mapToDtoWithPlayer_ShouldReturnTurnDto_WithPlayer() {
        //ARRANGE
        mockTurn.setPlayer(new Player());

        when(playerService.mapToDto(any(Player.class))).thenReturn(new PlayerDto(mockTurn.getPlayer().getId(), mockTurn.getPlayer().getMoney(), mockTurn.getPlayer().getUsername(), mockTurn.getPlayer().getPosition()));

        //ACT
        TurnDto result = turnService.mapToDtoWithPlayer(mockTurn);

        //ASSERT
        assertEquals(mockTurn.getId(), result.id());
        assertEquals(mockTurn.getMoveMade(), result.moveMade());
        assertEquals(mockTurn.getMoneyGambled(), result.moneyGambled());
        assertEquals(mockTurn.getPlayer().getId(), result.player().id());
        assertNull(result.roundId());
        assertEquals(mockTurn.getMadeInPhase(), result.madeInPhase());
    }
}