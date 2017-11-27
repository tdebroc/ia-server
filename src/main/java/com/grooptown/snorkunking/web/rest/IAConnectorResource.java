package com.grooptown.snorkunking.web.rest;

import com.grooptown.snorkunking.service.game.Game;
import com.grooptown.snorkunking.service.game.PlayerInstance;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by thibautdebroca on 26/11/2017.
 */
@RestController
@RequestMapping("/api/iaconnector")
public class IAConnectorResource {

    public static Map<Integer, Game> gamesMap = new HashMap<>();

    public static int NEXT_GAME_ID = 1;

    public static Map<String, PlayerInstance> playersInstances = new HashMap<>();

    @GetMapping("/game")
    public Game getNewGame(@RequestParam(required = false) Integer oxygenFactor,
                           @RequestParam(required = false) Integer caveCount) {
        int idGame = addGameToGamesMap(oxygenFactor, caveCount);
        return gamesMap.get(idGame);
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

    public int addGameToGamesMap(Integer oxygenFactor, Integer caveCount) {
        Game game = new Game(oxygenFactor == null ? 2 : oxygenFactor,
                            caveCount == null ? 3 : caveCount);
        int newLyGameId = NEXT_GAME_ID;
        game.setIdGame(newLyGameId);
        gamesMap.put(newLyGameId, game);
        NEXT_GAME_ID++;
        return newLyGameId;
    }
}
