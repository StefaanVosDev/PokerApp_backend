-- Seed data for achievement
INSERT INTO achievement (id, name, description, poker_points) VALUES
                                                                  (gen_random_uuid(), '5 Wins', 'Win 5 games', 50),
                                                                  (gen_random_uuid(), '10 Wins', 'Win 10 games', 100),
                                                                  (gen_random_uuid(), '20 Wins', 'Win 20 games', 200),
                                                                  (gen_random_uuid(), '50 Wins', 'Win 50 games', 500),
                                                                  (gen_random_uuid(), '100 Wins', 'Win 100 games', 1000),
                                                                  (gen_random_uuid(), 'Royal Flush', 'Achieve a Royal Flush', 100),
                                                                  (gen_random_uuid(), 'Flush', 'Achieve a Flush', 50),
                                                                  (gen_random_uuid(), 'Straight', 'Achieve a Straight', 30),
                                                                  (gen_random_uuid(), 'Royal Player', 'Achieve 10 Royal Flushes', 2000),
                                                                  (gen_random_uuid(), 'Flush God', 'Achieve 50 Flushes', 1500),
                                                                  (gen_random_uuid(), 'Straight Shooter', 'Achieve 100 Straight', 1000),
                                                                  (gen_random_uuid(), 'At least you tried', 'Participate in 100 games', 100),
                                                                  (gen_random_uuid(), 'Touch grass', 'Participate in 1000 games', 500);

-- Seed data for avatar
INSERT INTO avatar (id, name, image, price)
VALUES
    ('72f119be-f1a2-44ae-9831-7d2227a725b6', 'Angry Fish', 'https://storage.googleapis.com/poker_stacks/avatars/angryFish.svg', 50),
    (gen_random_uuid(), 'Bronze Bull', 'https://storage.googleapis.com/poker_stacks/avatars/bronzeBull.svg', 250),
    (gen_random_uuid(), 'Anime girl', 'https://storage.googleapis.com/poker_stacks/avatars/girlAvatar.svg', 50),
    (gen_random_uuid(), 'Goku', 'https://storage.googleapis.com/poker_stacks/avatars/gokuAvatar.svg', 150),
    (gen_random_uuid(), 'Pro gamer', 'https://storage.googleapis.com/poker_stacks/avatars/proAvatar.svg', 250),
    (gen_random_uuid(), 'Scary boy', 'https://storage.googleapis.com/poker_stacks/avatars/scaryBoy.svg', 250),
    (gen_random_uuid(), 'Silver Shark', 'https://storage.googleapis.com/poker_stacks/avatars/silverShark.svg', 250),
    (gen_random_uuid(), 'Basic man', 'https://storage.googleapis.com/poker_stacks/avatars/basicMan.svg', 250),
    ('91e4592f-4bb1-438c-b9f9-11c756fac0d4', 'duckpfp', 'https://storage.googleapis.com/poker_stacks/avatars/duckpfp.svg', 0);


INSERT INTO card (rank, suit, id)
VALUES
    -- Hearts (Suit = 0)
    (2, 0, gen_random_uuid()), (3, 0, gen_random_uuid()), (4, 0, gen_random_uuid()),
    (5, 0, gen_random_uuid()), (6, 0, gen_random_uuid()), (7, 0, gen_random_uuid()),
    (8, 0, gen_random_uuid()), (9, 0, gen_random_uuid()), (10, 0, gen_random_uuid()),
    (11, 0, gen_random_uuid()), (12, 0, gen_random_uuid()), (13, 0, gen_random_uuid()),
    (14, 0, gen_random_uuid()),

    -- Diamonds (Suit = 1)
    (2, 1, gen_random_uuid()), (3, 1, gen_random_uuid()), (4, 1, gen_random_uuid()),
    (5, 1, gen_random_uuid()), (6, 1, gen_random_uuid()), (7, 1, gen_random_uuid()),
    (8, 1, gen_random_uuid()), (9, 1, gen_random_uuid()), (10, 1, gen_random_uuid()),
    (11, 1, gen_random_uuid()), (12, 1, gen_random_uuid()), (13, 1, gen_random_uuid()),
    (14, 1, gen_random_uuid()),

    -- Clubs (Suit = 2)
    (2, 2, gen_random_uuid()), (3, 2, gen_random_uuid()), (4, 2, gen_random_uuid()),
    (5, 2, gen_random_uuid()), (6, 2, gen_random_uuid()), (7, 2, gen_random_uuid()),
    (8, 2, gen_random_uuid()), (9, 2, gen_random_uuid()), (10, 2, gen_random_uuid()),
    (11, 2, gen_random_uuid()), (12, 2, gen_random_uuid()), (13, 2, gen_random_uuid()),
    (14, 2, gen_random_uuid()),

    -- Spades (Suit = 3)
    (2, 3, gen_random_uuid()), (3, 3, gen_random_uuid()), (4, 3, gen_random_uuid()),
    (5, 3, gen_random_uuid()), (6, 3, gen_random_uuid()), (7, 3, gen_random_uuid()),
    (8, 3, gen_random_uuid()), (9, 3, gen_random_uuid()), (10, 3, gen_random_uuid()),
    (11, 3, gen_random_uuid()), (12, 3, gen_random_uuid()), (13, 3, gen_random_uuid()),
    (14, 3, gen_random_uuid());

-- Seed data for account
INSERT INTO account (level, active_avatar_id, id, email, username, name, age, city, gender, poker_points)
VALUES
    (1, (SELECT id FROM avatar WHERE name = 'Angry Fish'), gen_random_uuid(), 'player1@example.com', 'PlayerOne', 'Player One', '2000-01-01', 'CityOne', 0, 150),
    (5, (SELECT id FROM avatar WHERE name = 'Silver Shark'), gen_random_uuid(), 'player2@example.com', 'ProPlayer', 'Pro Player', '1995-05-05', 'CityTwo', 0, 100),
    (3, (SELECT id FROM avatar WHERE name = 'Bronze Bull'), gen_random_uuid(), 'player3@example.com', 'LuckyPlayer', 'Lucky Player', '1990-10-10', 'CityThree', 0, 200),
    (10, (SELECT id FROM avatar WHERE name = 'Silver Shark'), gen_random_uuid(), 'robbe.vanosselaer@student.kdg.be', 'Johnknee Rovo2.0', 'Johnknee Rovo', '2004-09-03', 'Duffel', 0, 200),
    (2, (SELECT id FROM avatar WHERE name = 'Silver Shark'), gen_random_uuid(), 'nofriends@student.kdg.be', 'nofriends', 'No Friends', '2001-01-01', 'CityFive', 0, 300);

INSERT INTO account (level, active_avatar_id, id, email, username, name, age, city, gender, poker_points)
VALUES
    (1, (select id from avatar where name = 'Bronze Bull') , '8a6047e7-89a9-4882-8336-438764a00b35', 'robbe@student.kdg.be', 'robbe', 'Robbe van Osselaer', '2004-09-03', 'Duffel', 0, 100),
    (1, (select id from avatar where name = 'Basic man') , gen_random_uuid(), 'anwar@student.kdg.be', 'anwar', 'Anwar Achbouni', '2000-12-05', 'Antwerpen', 0, 200),
    (1, (select id from avatar where name = 'Goku') , gen_random_uuid(), 'milan@student.kdg.be', 'milan', 'Milan Marschang', '2002-05-06', 'Schelle', 0, 200),
    (1, (select id from avatar where name = 'Anime girl') , gen_random_uuid(), 'stefaan@student.kdg.be', 'stefaan', 'Stefaan Vos', '2003-09-23', 'Brecht', 0, 200),
    (2, (select id from avatar where name = 'Scary boy'), '483630d2-e7fb-46ce-a871-a1713af1f4e1', 'afidullah.hamid@student.kdg.be', 'afi', 'Afidullah Hamid', '2001-06-02', 'Mechelen', 0, 250);

-- Ensure the counters for 'robbe@student.kdg.be' are updated (ID: '8a6047e7-89a9-4882-8336-438764a00b35')
INSERT INTO account_counters (account_id, counter_name, counter_value)
VALUES
    ('8a6047e7-89a9-4882-8336-438764a00b35', 'wins', 9),
    ('8a6047e7-89a9-4882-8336-438764a00b35', 'playedGames', 99),
    ('8a6047e7-89a9-4882-8336-438764a00b35', 'royal flushes', 0),
    ('8a6047e7-89a9-4882-8336-438764a00b35', 'flushes', 0),
    ('8a6047e7-89a9-4882-8336-438764a00b35', 'straights', 0);


-- Seed data for account_achievements
INSERT INTO account_achievements (account_id, achievements_id)
VALUES
    ((SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT id FROM achievement WHERE name = '10 Wins')),
    ((SELECT id FROM account WHERE email = 'player3@example.com'), (SELECT id FROM achievement WHERE name = '20 Wins')),
    ((SELECT id FROM account WHERE email = 'robbe@student.kdg.be'), (SELECT id FROM achievement WHERE name = '5 Wins'));

-- Seed data for account_avatars
INSERT INTO account_avatars (account_id, avatars_id)
VALUES
    ((SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT id FROM avatar WHERE name = 'Angry Fish')),
    ((SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT id FROM avatar WHERE name = 'Silver Shark')),
    ((SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'player3@example.com'), (SELECT id FROM avatar WHERE name = 'Bronze Bull')),
    ((SELECT id FROM account WHERE email = 'player3@example.com'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'robbe@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'Bronze Bull')),
    ((SELECT id FROM account WHERE email = 'robbe@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'afidullah.hamid@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'Scary boy')),
    ((SELECT id FROM account WHERE email = 'afidullah.hamid@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'robbe.vanosselaer@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'robbe.vanosselaer@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'Silver Shark')),
    ((SELECT id FROM account WHERE email = 'anwar@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'anwar@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'Basic man')),
    ((SELECT id FROM account WHERE email = 'milan@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'milan@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'Goku')),
    ((SELECT id FROM account WHERE email = 'stefaan@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'duckpfp')),
    ((SELECT id FROM account WHERE email = 'stefaan@student.kdg.be'), (SELECT id FROM avatar WHERE name = 'Anime girl'));

-- Seed data for account_friends
INSERT INTO account_friends (account_id, friends_id)
VALUES
    ((SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT id FROM account WHERE email = 'player2@example.com')),
    ((SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT id FROM account WHERE email = 'player1@example.com')),
    ((SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT id FROM account WHERE email = 'player3@example.com')),
    ((SELECT id FROM account WHERE email = 'player3@example.com'), (SELECT id FROM account WHERE email = 'player1@example.com'));



-- Seed data for direct messages
INSERT INTO direct_message (id ,sender_id, receiver_id, content, read)
VALUES
    (gen_random_uuid() ,(SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT id FROM account WHERE email = 'player2@example.com'), 'Hello Robbe!', true);


-- Seed data for configuration
INSERT INTO configuration (id, big_blind, small_blind, starting_chips, timer)
VALUES
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false),
    (gen_random_uuid(), 10, 5, 1000, false);

-- Seed data for game
INSERT INTO game (id, max_players, status, name, settings_id)
VALUES ('3e8c27df-6e15-426b-9c76-5825d61183f7', 2, 0, 'Game 1', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 LIMIT 1)),
       ('7fabf988-a888-4dc6-8423-4cd9f620ff00', 4, 0, 'Game 2', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 1 LIMIT 1)),
       ('6458725a-0945-480d-a006-b37ba0bc53af', 6, 0, 'Game 3', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 2 LIMIT 1)),
       (gen_random_uuid(), 3, 0, 'Game 4', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 3 LIMIT 1)),
       ('15947d5b-77db-456d-a7f7-bc8609b0646b', 3, 1, 'Game 5', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 4 LIMIT 1)),
       ('da06fffb-4855-4776-8a2c-62e39c3ebdd9', 5, 0, 'Game 6', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 5 LIMIT 1)),
       ('d687dbce-4435-490f-b92c-6741e363eace', 2, 0, 'Game 7', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 6 LIMIT 1)),
       ('df6b6682-0bdc-4c10-9471-d6c752963b1c', 2, 1, 'Game 8', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 7 LIMIT 1)),
       ('3ed2d7df-6e15-426b-9c76-5825d61183f7', 6, 2, 'Ended game', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 8 LIMIT 1)),
       ('30616ea3-7e32-40c6-91d5-2829a2ad615b', 5, 1, 'Game 9', (SELECT id FROM configuration WHERE big_blind = 10 AND small_blind = 5 OFFSET 9 LIMIT 1));

INSERT INTO notification (id, message, timestamp, account_id, achievement_id,type)
VALUES
    ('bd69e349-d077-4720-9eeb-e7508ea986e2', 'You got a new achievement!', NOW(), (SELECT id FROM account WHERE username = 'ProPlayer'), (SELECT id FROM achievement WHERE name = 'Flush' LIMIT 1),'AchievementNotification');

-- Seed data for notification
INSERT INTO notification (id, message, timestamp, account_id, game_id, type)
VALUES
    ('bd69cd49-d077-4720-9eeb-e7508ea986e2', 'Welcome to the game!', NOW(), (SELECT id FROM account WHERE username = 'ProPlayer'), (SELECT id FROM game WHERE name = 'Game 1' LIMIT 1),'GameNotification'),
    (gen_random_uuid(), 'Your game is ready!', NOW(), (SELECT id FROM account WHERE username = 'PlayerOne'), (SELECT id FROM game WHERE name = 'Game 1' LIMIT 1),'GameNotification');

INSERT INTO notification (id, timestamp, message, account_id, type, requesting_friend_id)
VALUES
    ('68c3fbe8-0d5c-4440-940c-24349f943462', NOW(), 'ProPlayer wants to be your friend', (SELECT id FROM account WHERE username = 'LuckyPlayer' LIMIT 1), 'FriendRequest', (SELECT id FROM account WHERE username = 'ProPlayer')),
    ('14e77d07-38a9-4819-940e-9627495ebef0', NOW(), 'PlayerOne wants to be your friend', (SELECT id FROM account WHERE username = 'LuckyPlayer' LIMIT 1), 'FriendRequest', (SELECT id FROM account WHERE username = 'PlayerOne')),
    (gen_random_uuid(), NOW(), 'Robbe wants to be your friend', (SELECT id FROM account WHERE username = 'PlayerOne' LIMIT 1), 'FriendRequest', (SELECT id FROM account WHERE username = 'robbe'));

insert into notification(id, timestamp, message, account_id, type, sender, game_id)
    VALUES
    ('09e58459-86af-4cd6-9e42-055f5d652831', NOW(), 'Robbe has invited you to game', (SELECT id FROM account WHERE username = 'PlayerOne' LIMIT 1), 'InviteNotification', (SELECT username FROM account WHERE username = 'robbe'), (SELECT id FROM game WHERE name = 'Game 1' LIMIT 1));

-- Seed data for player
INSERT INTO player (id, money, game_id, position, account_id, username)
VALUES ('fbe9bdbf-4329-4fea-9bed-0449d5677804', 500, (SELECT id FROM game WHERE name = 'Game 1' LIMIT 1), 0, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       (gen_random_uuid(), 500, (SELECT id FROM game WHERE name = 'Game 1' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT username FROM account WHERE email = 'player2@example.com')),
       (gen_random_uuid(), 1000, (SELECT id FROM game WHERE name = 'Game 2' LIMIT 1), 0, (SELECT id FROM account WHERE email = 'player3@example.com'), (SELECT username FROM account WHERE email = 'player3@example.com')),
       (gen_random_uuid(), 650, (SELECT id FROM game WHERE name = 'Game 2' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       (gen_random_uuid(), 601, (SELECT id FROM game WHERE name = 'Game 2' LIMIT 1), 2, (SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT username FROM account WHERE email = 'player2@example.com')),
       (gen_random_uuid(), 1200, (SELECT id FROM game WHERE name = 'Game 2' LIMIT 1), 3, (SELECT id FROM account WHERE email = 'robbe.vanosselaer@student.kdg.be'), (SELECT username FROM account WHERE email = 'robbe.vanosselaer@student.kdg.be')),
       (gen_random_uuid(), 1000, (SELECT id FROM game WHERE name = 'Game 3' LIMIT 1), 0, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       (gen_random_uuid(), 810, (SELECT id FROM game WHERE name = 'Game 3' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT username FROM account WHERE email = 'player2@example.com')),
       (gen_random_uuid(), 10, (SELECT id FROM game WHERE name = 'Game 4' LIMIT 1), 0, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       (gen_random_uuid(), 25, (SELECT id FROM game WHERE name = 'Game 4' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT username FROM account WHERE email = 'player2@example.com')),
       (gen_random_uuid(), 100, (SELECT id FROM game WHERE name = 'Game 4' LIMIT 1), 2, (SELECT id FROM account WHERE email = 'player3@example.com'), (SELECT username FROM account WHERE email = 'player3@example.com')),
       (gen_random_uuid(), 1910, (SELECT id FROM game WHERE name = 'Game 5' LIMIT 1), 0, (SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT username FROM account WHERE email = 'player2@example.com')),
       (gen_random_uuid(), 1920, (SELECT id FROM game WHERE name = 'Game 6' LIMIT 1), 0, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       ('b6a8c818-5486-47ea-8eca-3c9aad524edf', 10000, (SELECT id FROM game WHERE name = 'Game 7' LIMIT 1), 0, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       ('a67a46f6-550f-4aaf-b2ee-adb0446db889', 11000, (SELECT id FROM game WHERE name = 'Game 7' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT username FROM account WHERE email = 'player2@example.com')),
       ('bbc1734a-f56c-4392-a83a-b7587787f727', 8000, (SELECT id FROM game WHERE name = 'Game 8' LIMIT 1), 0, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       ('be264183-a2b9-4b90-8fb4-84840a21d011', 9000, (SELECT id FROM game WHERE name = 'Game 8' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT username FROM account WHERE email = 'player2@example.com')),
       ('a54a46f6-550f-4aaf-b2ee-adb0446db889', 300, (SELECT id FROM game WHERE name = 'Ended game' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       (gen_random_uuid(), 456, (SELECT id FROM game WHERE name = 'Game 9' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player1@example.com'), (SELECT username FROM account WHERE email = 'player1@example.com')),
       (gen_random_uuid(), 465, (SELECT id FROM game WHERE name = 'Game 9' LIMIT 1), 1, (SELECT id FROM account WHERE email = 'player2@example.com'), (SELECT username FROM account WHERE email = 'player2@example.com'));

-- Update the game to set the winner_id
UPDATE game
SET winner_id = 'a54a46f6-550f-4aaf-b2ee-adb0446db889'
WHERE id = '3ed2d7df-6e15-426b-9c76-5825d61183f7';

-- Seed data for account_players
INSERT INTO account_players (account_id, players_id)
VALUES
    ((SELECT id FROM account WHERE email = 'player1@example.com' LIMIT 1), (SELECT id FROM player WHERE money = 500 LIMIT 1)),
    ((SELECT id FROM account WHERE email = 'player2@example.com' LIMIT 1), (SELECT id FROM player WHERE money = 500 OFFSET 1 LIMIT 1)),
    ((SELECT id FROM account WHERE email = 'player3@example.com' LIMIT 1), (SELECT id FROM player WHERE money = 1000 LIMIT 1)),
    ((SELECT id FROM account WHERE email = 'player1@example.com' LIMIT 1), (SELECT id FROM player WHERE money = 10 LIMIT 1)),
    ((SELECT id FROM account WHERE email = 'player2@example.com' LIMIT 1), (SELECT id FROM player WHERE money = 25 LIMIT 1)),
    ((SELECT id FROM account WHERE email = 'player3@example.com' LIMIT 1), (SELECT id FROM player WHERE money = 100 LIMIT 1));

-- Seed data for player_hand
INSERT INTO player_hand (hand_id, player_id)
VALUES
    ((SELECT id FROM card WHERE rank = 2 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE money = 500 LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 10 AND suit = 1 LIMIT 1), (SELECT id FROM player WHERE money = 500 OFFSET 1 LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 5 AND suit = 2 LIMIT 1), (SELECT id FROM player WHERE money = 1000 LIMIT 1)),

    ((SELECT id FROM card WHERE rank = 9 AND suit = 3 LIMIT 1), (SELECT id FROM player WHERE money = 10 LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 9 AND suit = 1 LIMIT 1), (SELECT id FROM player WHERE money = 10 LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 10 AND suit = 3 LIMIT 1), (SELECT id FROM player WHERE money = 25 LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 3 AND suit = 1 LIMIT 1), (SELECT id FROM player WHERE money = 25 LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 12 AND suit = 2 LIMIT 1), (SELECT id FROM player WHERE money = 100 LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 9 AND suit = 2 LIMIT 1), (SELECT id FROM player WHERE money = 100 LIMIT 1)),

       ((SELECT id FROM card WHERE rank = 8 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE money = 10000 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 4 AND suit = 1 LIMIT 1), (SELECT id FROM player WHERE money = 10000 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 9 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE money = 11000 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 9 AND suit = 1 LIMIT 1), (SELECT id FROM player WHERE money = 11000 LIMIT 1)),

       ((SELECT id FROM card WHERE rank = 8 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE money = 8000 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 4 AND suit = 1 LIMIT 1), (SELECT id FROM player WHERE money = 8000 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 9 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE money = 9000 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 9 AND suit = 1 LIMIT 1), (SELECT id FROM player WHERE money = 9000 LIMIT 1));


-- Seed data for round
INSERT INTO round (id, phase, game_id, dealer_index, created_at)
VALUES ('668bbee6-eeca-4123-a5d5-a3217f96c26a', 1, (SELECT id FROM game WHERE name = 'Game 1' LIMIT 1), 0, NOW()),
       (gen_random_uuid(), 0, (SELECT id FROM game WHERE name = 'Game 2' LIMIT 1), 0, NOW()),
       (gen_random_uuid(), 2, (SELECT id FROM game WHERE name = 'Game 3' LIMIT 1), 0, NOW()),
       ('0e6ea81f-5527-4aef-98cb-2afb233fd8af', 4, (SELECT id FROM game WHERE name = 'Game 4' LIMIT 1), 0, NOW()),
       (gen_random_uuid(), 0, (SELECT id FROM game WHERE name = 'Game 5' LIMIT 1), 0, NOW()),
       ('cccf7b01-4f8e-4c18-ae6b-3af5c663dfb6', 3, (SELECT id FROM game WHERE name = 'Game 6' LIMIT 1), 0, NOW()),
       ('c01cc034-0d9f-4319-8091-0672b528f942', 4, (SELECT id FROM game WHERE name = 'Game 7' LIMIT 1), 0, NOW()),
       ('33c7aef3-c4bc-4235-a4b4-74b305d84c33', 4, (SELECT id FROM game WHERE name = 'Game 8' LIMIT 1), 1, NOW()),
       (gen_random_uuid(), 3, (SELECT id FROM game WHERE name = 'Ended game' LIMIT 1), 0, NOW()),
       ('61870bbf-7e8a-4157-b524-c8d4afb5aee4', 4, (SELECT id FROM game WHERE name ='Game 9' LIMIT 1), 1, NOW());

-- Seed data for round_community_cards
INSERT INTO round_community_cards (community_cards_id, round_id)
VALUES ((SELECT id FROM card WHERE rank = 2 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE phase = 1 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 5 AND suit = 2 LIMIT 1), (SELECT id FROM round WHERE phase = 2 LIMIT 1)),

       ((SELECT id FROM card WHERE rank = 13 AND suit = 2 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 11 AND suit = 2 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 10 AND suit = 2 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 3 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 2 AND suit = 1 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1)),

       ((SELECT id FROM card WHERE rank = 7 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 7 AND suit = 1 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 14 AND suit = 3 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 3 AND suit = 3 LIMIT 1),(SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 12 AND suit = 3 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1)),

       ((SELECT id FROM card WHERE rank = 2 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 3 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 4 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 5 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1)),
       ((SELECT id FROM card WHERE rank = 6 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1));

insert into round_deck(deck_id, round_id)
VALUES
    ((SELECT id FROM card WHERE rank = 2 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 3 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 4 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 5 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 6 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 7 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 8 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 9 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 10 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 11 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 12 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 13 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a'),
    ((SELECT id FROM card WHERE rank = 14 AND suit = 0 LIMIT 1), '668bbee6-eeca-4123-a5d5-a3217f96c26a');

-- Seed data for turn
INSERT INTO turn (id, move_made, money_gambled, player_id, round_id, created_at)
VALUES (gen_random_uuid(), 1, 100, (SELECT id FROM player WHERE money = 500 LIMIT 1), (SELECT id FROM round WHERE phase = 1 LIMIT 1), NOW()),
       ('42fb6a81-96ef-46d0-95d9-ff4d251c1530', 0, 0, (SELECT id FROM player WHERE money = 500 OFFSET 1 LIMIT 1), (SELECT id FROM round WHERE phase = 1 LIMIT 1), NOW()),

       (gen_random_uuid(), 1, 200, (SELECT id FROM player WHERE money = 1000 LIMIT 1), (SELECT id FROM round WHERE phase = 2 LIMIT 1), NOW()),
       (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE money = 810 LIMIT 1), (SELECT id FROM round WHERE phase = 2 LIMIT 1), NOW()),

       (gen_random_uuid(), 4, 10, (SELECT id FROM player WHERE money = 10 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1), NOW()),
       (gen_random_uuid(), 4, 25, (SELECT id FROM player WHERE money = 25 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1), NOW()),
       (gen_random_uuid(), 2, 25, (SELECT id FROM player WHERE money = 100 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1), NOW()),
       (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE money = 10 LIMIT 1), (SELECT id FROM round WHERE phase = 4 LIMIT 1), NOW()),

       (gen_random_uuid(), 1, 110, (SELECT id FROM player WHERE money = 10000 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1), NOW()),
       (gen_random_uuid(), 2, 110, (SELECT id FROM player WHERE money = 11000 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1), NOW()),
       (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE money = 10000 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1), NOW()),

       (gen_random_uuid(), 1, 150, (SELECT id FROM player WHERE money = 8000 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1), NOW()),
       (gen_random_uuid(), 2, 150, (SELECT id FROM player WHERE money = 9000 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1), NOW()),
       ('6021f85d-60a0-4168-a754-b43aa74618f2', 0, 0, (SELECT id FROM player WHERE money = 8000 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1), NOW()),

       (gen_random_uuid(), 5, 0, (SELECT id FROM player WHERE money = 1000 OFFSET 1 LIMIT 1), (SELECT id FROM round WHERE phase = 0 LIMIT 1), NOW()),
       (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE money = 650 LIMIT 1), (SELECT id FROM round WHERE phase = 0 LIMIT 1), NOW()),

       (gen_random_uuid(), 1, 100, (SELECT id FROM player WHERE money = 1910 LIMIT 1), (SELECT id FROM round WHERE phase = 0 OFFSET 1 LIMIT 1), NOW()),
       (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE money = 1910 LIMIT 1), (SELECT id FROM round WHERE phase = 0 OFFSET 1 LIMIT 1), NOW()),

       (gen_random_uuid(), 6, 5, (SELECT id FROM player WHERE money = 300 LIMIT 1), (SELECT id FROM round WHERE phase = 3 OFFSET 1 LIMIT 1), NOW()),

       ('8b6fe2a7-0c4e-4f8c-86dc-8283b618a58d', 2, 150, (SELECT id FROM player WHERE money = 456 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 3 LIMIT 1), NOW()),
       (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE money = 465 LIMIT 1), (SELECT id FROM round WHERE phase = 4 OFFSET 3 LIMIT 1), NOW());

-- Seed data for player_turns
INSERT INTO player_turns (player_id, turns_id)
VALUES ((SELECT id FROM player WHERE money = 500 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 100 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 500 OFFSET 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 LIMIT 1)),

       ((SELECT id FROM player WHERE money = 1000 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 200 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 810 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 1 LIMIT 1)),

       ((SELECT id FROM player WHERE money = 10 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 10 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 25 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 25 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 100 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 25 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 10 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 2 LIMIT 1)),

       ((SELECT id FROM player WHERE money = 10000 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 110 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 11000 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 110 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 10000 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 3 LIMIT 1)),

       ((SELECT id FROM player WHERE money = 8000 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 150 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 9000 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 150 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 8000 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 4 LIMIT 1)),

       ((SELECT id FROM player WHERE money = 1000 OFFSET 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 5 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 650 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 6 LIMIT 1)),

       ((SELECT id FROM player WHERE money = 1910 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 100 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 1910 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 7 LIMIT 1)),

       ((SELECT id FROM player WHERE money = 300 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 5 LIMIT 1)),

       ((SELECT id FROM player WHERE money = 456 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 150 OFFSET 2 LIMIT 1)),
       ((SELECT id FROM player WHERE money = 465 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 8 LIMIT 1));


-- Seed data for round_turns
INSERT INTO round_turns (round_id, turns_id)
VALUES ((SELECT id FROM round WHERE phase = 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 100 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 LIMIT 1)),

       ((SELECT id FROM round WHERE phase = 2 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 200 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 2 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 1 LIMIT 1)),

       ((SELECT id FROM round WHERE phase = 4 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 10 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 4 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 25 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 4 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 25 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 4 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 2 LIMIT 1)),

       ((SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 110 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 110 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 4 OFFSET 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 3 LIMIT 1)),

       ((SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 150 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 150 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 4 OFFSET 2 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 4 LIMIT 1)),

       ((SELECT id FROM round WHERE phase = 0 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 5 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 0 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 6 LIMIT 1)),

       ((SELECT id FROM round WHERE phase = 0 OFFSET 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 100 OFFSET 1 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 0 OFFSET 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 7 LIMIT 1)),

       ((SELECT id FROM round WHERE phase = 3 OFFSET 1 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 5 LIMIT 1)),

       ((SELECT id FROM round WHERE phase = 4 OFFSET 3 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 150 OFFSET 2 LIMIT 1)),
       ((SELECT id FROM round WHERE phase = 4 OFFSET 3 LIMIT 1), (SELECT id FROM turn WHERE money_gambled = 0 OFFSET 8 LIMIT 1));


INSERT INTO game_message(id, player_id, content, timestamp, game_id)
VALUES (gen_random_uuid(), (SELECT id FROM player WHERE money = 500 OFFSET 1 LIMIT 1), 'Hoi', NOW(), (SELECT id FROM game WHERE name = 'Game 4' LIMIT 1));


INSERT INTO configuration (id, big_blind, small_blind, starting_chips, timer)
VALUES
    (gen_random_uuid(), 100, 50, 1000, false),
    (gen_random_uuid(), 200, 100, 2000, false),
    (gen_random_uuid(), 300, 150, 3000, false);

-- Seed data for games
INSERT INTO game (id, max_players, status, name, settings_id)
VALUES
    (gen_random_uuid(), 2, 1, 'Royal Flush', (SELECT id FROM configuration WHERE big_blind = 100 AND small_blind = 50 LIMIT 1)),
    (gen_random_uuid(), 2, 1, 'Flush', (SELECT id FROM configuration WHERE big_blind = 200 AND small_blind = 100 LIMIT 1)),
    (gen_random_uuid(), 2, 1, 'Straight', (SELECT id FROM configuration WHERE big_blind = 300 AND small_blind = 150 LIMIT 1));
-- Seed data for players
INSERT INTO player (id, money, game_id, position, account_id, username)
VALUES
    (gen_random_uuid(), 1000, (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1), 0, (SELECT id FROM account WHERE username = 'robbe'), 'robbe'),
    (gen_random_uuid(), 1000, (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1), 1, (SELECT id FROM account WHERE username = 'afi'), 'afi'),
    (gen_random_uuid(), 1000, (SELECT id FROM game WHERE name = 'Flush' LIMIT 1), 0, (SELECT id FROM account WHERE username = 'robbe'), 'robbe'),
    (gen_random_uuid(), 1000, (SELECT id FROM game WHERE name = 'Flush' LIMIT 1), 1, (SELECT id FROM account WHERE username = 'afi'), 'afi'),
    (gen_random_uuid(), 1000, (SELECT id FROM game WHERE name = 'Straight' LIMIT 1), 0, (SELECT id FROM account WHERE username = 'robbe'), 'robbe'),
    (gen_random_uuid(), 1000, (SELECT id FROM game WHERE name = 'Straight' LIMIT 1), 1, (SELECT id FROM account WHERE username = 'afi'), 'afi');

-- Seed data for rounds
INSERT INTO round (id, phase, game_id, dealer_index, created_at)
VALUES
    (gen_random_uuid(), 4, (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1), 0, NOW()),
    (gen_random_uuid(), 4, (SELECT id FROM game WHERE name = 'Flush' LIMIT 1), 0, NOW()),
    (gen_random_uuid(), 4, (SELECT id FROM game WHERE name = 'Straight' LIMIT 1), 0, NOW());

-- Seed data for turns
INSERT INTO turn (id, move_made, money_gambled, player_id, round_id, created_at)
VALUES
    (gen_random_uuid(), 1, 100, (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) AND username = 'robbe' LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1), NOW()),
    (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) AND username = 'afi' LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1), NOW()),
    (gen_random_uuid(), 1, 100, (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) AND username = 'robbe' LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1), NOW()),
    (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) AND username = 'afi' LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1), NOW()),
    (gen_random_uuid(), 1, 100, (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) AND username = 'robbe' LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1), NOW()),
    (gen_random_uuid(), 0, 0, (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) AND username = 'afi' LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1), NOW());

-- Link turns to rounds
INSERT INTO round_turns (round_id, turns_id)
VALUES
    ((SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1), (SELECT id FROM turn WHERE round_id = (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1) LIMIT 1)),
    ((SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1), (SELECT id FROM turn WHERE round_id = (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1) OFFSET 1 LIMIT 1)),
    ((SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1), (SELECT id FROM turn WHERE round_id = (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1) LIMIT 1)),
    ((SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1), (SELECT id FROM turn WHERE round_id = (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1) OFFSET 1 LIMIT 1)),
    ((SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1), (SELECT id FROM turn WHERE round_id = (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1) LIMIT 1)),
    ((SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1), (SELECT id FROM turn WHERE round_id = (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1) OFFSET 1 LIMIT 1));

-- Seed data for player hands
-- Royal Flush for robbe
INSERT INTO player_hand (hand_id, player_id)
VALUES
    ((SELECT id FROM card WHERE rank = 10 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) AND username = 'robbe' LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 11 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) AND username = 'robbe' LIMIT 1));

-- Flush for afi
INSERT INTO player_hand (hand_id, player_id)
VALUES
    ((SELECT id FROM card WHERE rank = 2 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) AND username = 'afi' LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 4 AND suit = 0 LIMIT 1), (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) AND username = 'afi' LIMIT 1));

-- Straight for robbe
INSERT INTO player_hand (hand_id, player_id)
VALUES
    ((SELECT id FROM card WHERE rank = 10 AND suit = 2 LIMIT 1), (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) AND username = 'robbe' LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 11 AND suit = 3 LIMIT 1), (SELECT id FROM player WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) AND username = 'robbe' LIMIT 1));

-- Seed data for community cards
INSERT INTO round_community_cards (community_cards_id, round_id)
VALUES
    ((SELECT id FROM card WHERE rank = 8 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 5 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 12 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 13 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 14 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Royal Flush' LIMIT 1) LIMIT 1));

-- Seed data for community cards
INSERT INTO round_community_cards (community_cards_id, round_id)
VALUES
    ((SELECT id FROM card WHERE rank = 8 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 7 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 9 AND suit = 1 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 13 AND suit = 1 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 14 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Flush' LIMIT 1) LIMIT 1));

-- Seed data for community cards
INSERT INTO round_community_cards (community_cards_id, round_id)
VALUES
    ((SELECT id FROM card WHERE rank = 8 AND suit = 1 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 5 AND suit = 1 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 12 AND suit = 1 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 13 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1)),
    ((SELECT id FROM card WHERE rank = 14 AND suit = 0 LIMIT 1), (SELECT id FROM round WHERE game_id = (SELECT id FROM game WHERE name = 'Straight' LIMIT 1) LIMIT 1));
