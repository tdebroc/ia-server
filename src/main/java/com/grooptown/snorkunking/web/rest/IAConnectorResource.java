package com.grooptown.snorkunking.web.rest;

import com.grooptown.snorkunking.service.game.Game;
import com.grooptown.snorkunking.service.game.Message;
import com.grooptown.snorkunking.service.game.Player;
import com.grooptown.snorkunking.service.game.PlayerInstance;
import com.grooptown.snorkunking.service.game.moves.Move;
import com.grooptown.snorkunking.service.game.moves.MoveManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public static Map<Integer, Game> gamesMap = new HashMap<>();

    public static int NEXT_GAME_ID = 1;

    public static Map<String, PlayerInstance> playersInstances = new HashMap<>();

    public IAConnectorResource() {
        createNewGame(3, 3);

    }

    @GetMapping("/game")
    public Game createNewGame(@RequestParam(required = false) Integer oxygenFactor,
                           @RequestParam(required = false) Integer caveCount) {
        int idGame = addGameToGamesMap(oxygenFactor, caveCount);
        return gamesMap.get(idGame);
    }

    @GetMapping("/game/{idGame}")
    public Game getGame(@PathVariable Integer idGame) {
        System.out.println("Get Game: " + idGame);
        return gamesMap.get(idGame);
    }


    @GetMapping("/games")
    public Set<Integer> getNewGame() {
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
        // refreshGame(game);
        playersInstances.put(userId, playerInstance);
        return playerInstance;
    }

    @GetMapping(value = "/startGame")
    public boolean startGame(@RequestParam(value = "idGame") int idGame) {
        gamesMap.get(idGame).startGame();
        return true;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/sendMove")
    public ResponseEntity<Message> sendMove(@RequestParam(value = "playerUUID") String playerUUID,
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
        game.getCurrentStage().prepareMove(game);
        refreshGame(game);
        return sendValidResponse("OK");
    }

    private void refreshGame(Game game) {
        // TODO
    }

    public int addGameToGamesMap(Integer oxygenFactor, Integer caveCount) {
        Game game = new Game(oxygenFactor == null ? 2 : oxygenFactor,
                            caveCount == null ? 3 : caveCount);
        int newLyGameId = NEXT_GAME_ID;
        game.setIdGame(newLyGameId);
        gamesMap.put(newLyGameId, game);
        NEXT_GAME_ID++;
        return newLyGameId;
    }

    public ResponseEntity<Message> sendBadRequest(String message) {
        return new ResponseEntity<>(new Message(message), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Message> sendValidResponse(String message) {
        return new ResponseEntity<>(new Message(message), HttpStatus.OK);
    }



}
