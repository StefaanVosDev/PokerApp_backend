package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.DirectMessageDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.DirectMessage;
import be.kdg.poker.domain.Game;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.DirectMessageRepository;
import be.kdg.poker.repositories.GameRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final AccountRepository accountRepository;
    private final GameRepository gameRepository;

    public DirectMessageService(DirectMessageRepository directMessageRepository, AccountRepository accountRepository, GameRepository gameRepository) {
        this.directMessageRepository = directMessageRepository;
        this.accountRepository = accountRepository;
        this.gameRepository = gameRepository;
    }


    public List<DirectMessageDto> getDirectMessages(String receiverUsername, String senderUsername) {
        Account sender = accountRepository.findAccountByUsername(senderUsername)
                .orElseThrow(() -> new AccountNotFoundException("Account with username " + senderUsername + " not found"));

        Account receiver = accountRepository.findAccountByUsername(receiverUsername)
                .orElseThrow(() -> new AccountNotFoundException("Account with username " + receiverUsername + " not found"));

        Optional<List<DirectMessage>> directMessages = directMessageRepository.findDirectMessagesBySenderAndReceiver(sender, receiver);

        return directMessages.map(messages -> messages.stream()
                        .map(message -> new DirectMessageDto(message.getSender().getUsername(), message.getReceiver().getUsername(), message.getContent(), message.getGameId(), message.getTimestamp(), message.isRead()))
                        .toList())
                .orElse(List.of());
    }

    public void sendDirectMessage(DirectMessageDto directMessageDto) {
        Account sender = accountRepository.findAccountByUsername(directMessageDto.sender())
                .orElseThrow(() -> new AccountNotFoundException("Account with username " + directMessageDto.sender() + " not found"));

        Account receiver = accountRepository.findAccountByUsername(directMessageDto.receiver())
                .orElseThrow(() -> new AccountNotFoundException("Account with username " + directMessageDto.receiver() + " not found"));

        DirectMessage directMessage = new DirectMessage(sender, receiver, directMessageDto.message());

        directMessageRepository.save(directMessage);
    }

    public void sendGameInvites(String username, UUID gameId, List<String> friendUsernames) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game with id " + gameId + " not found"));

        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException("Account with username " + username + " not found"));

        for (String friendUsername : friendUsernames) {
            Account friendAccount = accountRepository.findAccountByUsername(friendUsername)
                    .orElseThrow(() -> new AccountNotFoundException("Account with username " + username + " not found"));

            DirectMessage directMessage = new DirectMessage(account, friendAccount, "You have been invited to game " + game.getName(), gameId);
            directMessageRepository.save(directMessage);
        }
    }

    @Transactional
    public boolean markAllAsRead(String receiver, String sender) throws AccountNotFoundException {
       return accountRepository.findAccountByUsername(receiver).flatMap(
                        receiverAccount -> accountRepository.findAccountByUsername(sender).map(
                                    senderAccount -> directMessageRepository.findDirectMessagesBySender(senderAccount, receiverAccount).map(
                                            messages -> {
                                                log.info("successfully found DMs between {} and {}: marking as read", receiver, sender);
                                                messages.forEach(message -> message.setRead(true));
                                                directMessageRepository.saveAll(messages);
                                                return true;
                                            }
                                    )
                                )
                                .orElseThrow(() -> new AccountNotFoundException("account of sender with username " + sender + " does not exits"))
                )
                .orElseThrow(() -> new AccountNotFoundException("account of receiver with username " + receiver + " does not exist"));
    }
}
