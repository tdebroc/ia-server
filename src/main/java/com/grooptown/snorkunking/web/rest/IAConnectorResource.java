package com.grooptown.snorkunking.web.rest;

import com.grooptown.snorkunking.service.game.Game;
import com.grooptown.snorkunking.service.game.Message;
import com.grooptown.snorkunking.service.game.Player;
import com.grooptown.snorkunking.service.game.PlayerInstance;
import com.grooptown.snorkunking.service.game.connector.MessageResponse;
import com.grooptown.snorkunking.service.game.moves.Move;
import com.grooptown.snorkunking.service.game.moves.MoveManager;
import com.grooptown.snorkunking.service.game.moves.RecordMove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.grooptown.snorkunking.service.game.moves.MoveManager.getNextMove;

/**
 * Created by thibautdebroca on 26/11/2017.
 */
@RestController
@RequestMapping("/api/iaconnector")
public class IAConnectorResource {

    @Autowired
    SimpMessageSendingOperations messagingTemplate;

    public static Map<Integer, Game> gamesMap = new HashMap<>();

    public static int NEXT_GAME_ID = 1;

    public static Map<String, PlayerInstance> playersInstances = new HashMap<>();

    public IAConnectorResource() {
        init();
    }

    @GetMapping("/init")
    public void init() {
        NEXT_GAME_ID = 1;
        gamesMap = new HashMap<>();
        createNewGame(2.0, 3, 3);
    }

    @GetMapping("/game")
    public Game createNewGame(@RequestParam(required = false) Double oxygenFactor,
                              @RequestParam(required = false) Integer caveCount,
                              @RequestParam(required = false) Integer caveWidth) {
        int idGame = addGameToGamesMap(oxygenFactor, caveCount, caveWidth);
        refreshGames();
        return gamesMap.get(idGame);
    }

    @GetMapping("/game/{idGame}")
    public Game getGame(@PathVariable Integer idGame) {
        System.out.println("Get Game: " + idGame);
        return gamesMap.get(idGame);
    }


    @GetMapping("/games")
    public Set<Integer> getGames() {
        return gamesMap.keySet();
    }

    @GetMapping(value = "/addPlayer")
    public PlayerInstance addPlayer(@RequestParam(value = "idGame") int idGame,
                                    @RequestParam(value = "playerName", required = false) String playerName) {
        Game game = gamesMap.get(idGame);
        if (game.isStarted() ||
            game.getPlayers().size() >= Game.MAX_NUM_PLAYER) {
            return null;
        }
        String userId;
        do {
            userId = UUID.randomUUID().toString();
        } while (playersInstances.containsKey(userId));

        PlayerInstance playerInstance = new PlayerInstance(idGame, game.getPlayers().size(), userId);
        game.addPlayer(playerName);
        refreshGame(game);
        playersInstances.put(userId, playerInstance);
        return playerInstance;
    }

    @GetMapping(value = "/startGame")
    public boolean startGame(@RequestParam(value = "idGame") int idGame) {
        Game game = gamesMap.get(idGame);
        game.startGame();
        refreshGame(game);
        return true;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/sendMove")
    public ResponseEntity<MessageResponse> sendMove(@RequestParam(value = "playerUUID") String playerUUID,
                                                    @RequestParam(value = "move") String moveString) {
        System.out.println(playerUUID);
        PlayerInstance playerInstance = playersInstances.get(playerUUID);
        System.out.println(playerInstance);
        System.out.println(playersInstances);
        if (playerInstance == null) {
            return sendBadRequest("Unknown User");
        }
        Game game = gamesMap.get(playerInstance.getIdGame());
        if (game == null) {
            return sendBadRequest("Unknown Game");
        }
        if (!game.isStarted()) {
            return sendBadRequest("Game has not started.");
        }
        if (game.isFinished()) {
            return sendBadRequest("Game is Finished.");
        }
        if (!playersInstances.containsKey(playerUUID)) {
            return sendBadRequest("Unknown player");
        }
        if (game.getCurrentIdPlayerTurn() != playerInstance.getIdPlayer()) {
            return sendBadRequest("It's not the turn of player " + (playerInstance.getIdPlayer() + 1));
        }
        Move nextMove = getNextMove(moveString);

        Player player = playerInstance.getPlayerFromInstance(game);

        if (!MoveManager.isValidMove(nextMove, game, player)) {
            return sendBadRequest("Wrong Move");
        }

        nextMove.playMove(game, player);
        RecordMove recordMove = new RecordMove(moveString, playerInstance.getIdPlayer());
        game.getMoveList().add(recordMove);
        game.getCurrentStage().prepareMove(game);
        refreshGame(game);
        return sendValidResponse("OK");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getOpponentMoves")
    public ResponseEntity<Game> getOpponentMoves(@RequestParam(value = "playerUUID") String playerUUID) throws URISyntaxException, InterruptedException {
        PlayerInstance playerInstance = playersInstances.get(playerUUID);
        if (playerInstance == null) {
            return null;
        }
        Game game = gamesMap.get(playerInstance.getIdGame());
        int timeRequest = 0;
        try {
            while (game.getCurrentIdPlayerTurn() != playerInstance.getIdPlayer() || !game.isStarted()) {
                int sleepDuration = 100;
                // System.out.println("Sleeping for " + sleepDuration + "ms");
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore interrupted status
                }
                timeRequest += sleepDuration;
                if (timeRequest > 60 * 1000 * 10) {
                    return null;
                }
            }
        } catch (RuntimeException e) {
            System.out.println("Run time catched for " + e + "");
        }
        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    public int addGameToGamesMap(Double oxygenFactor, Integer caveCount, Integer caveWidth) {
        System.out.println("oxygenFactor=" + oxygenFactor + " and caveCount=" + caveCount);
        Game game = new Game(oxygenFactor == null ? 2.0 : oxygenFactor,
            caveCount == null ? 3 : caveCount,
            caveWidth == null ? 1 : caveWidth);
        int newLyGameId = NEXT_GAME_ID;
        game.setIdGame(newLyGameId);
        gamesMap.put(newLyGameId, game);
        NEXT_GAME_ID++;
        return newLyGameId;
    }

    public ResponseEntity<MessageResponse> sendBadRequest(String message) {
        return new ResponseEntity<>(new MessageResponse(message, null), HttpStatus.OK);
    }

    public ResponseEntity<MessageResponse> sendValidResponse(String message) {
        return new ResponseEntity<>(new MessageResponse(null, message), HttpStatus.OK);
    }

    //==================================================================================================================
    //= Sockets
    //==================================================================================================================
    private void refreshGames() {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/refreshGames", getGames());
        }
    }

    private void refreshGame(Game game) {
        messagingTemplate.convertAndSend("/topic/refreshGame", game);
    }


}
