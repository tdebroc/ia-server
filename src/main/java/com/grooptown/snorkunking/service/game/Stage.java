package com.grooptown.snorkunking.service.game;

import com.grooptown.snorkunking.service.game.moves.Move;
import com.grooptown.snorkunking.service.game.moves.MoveManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by thibautdebroca on 11/11/2017.
 */
public class Stage {

    private int oxygen;

    private int turn = 0;

    public void playStage(Game game) {
        List<Player> currentPlayers = new ArrayList<>();
        putPlayersAtSurface(game);
        while (oxygen > 0) {
            if (currentPlayers.size() == 0) {
                currentPlayers.addAll(game.getPlayers());
                turn++;
            }
            Player player = pickNextPlayer(currentPlayers);
            game.displayGame();
            Move move = MoveManager.askNextMove(game, player);
            move.playMove(game, player);
        }
        makeChestFolds(game);
        calculateScore(game);
        removeLevelsWithNoChests(game);
    }

    private void removeLevelsWithNoChests(Game game) {
        for (Cave cave : game.getCaves()) {
            List<Level> levels = cave.getLevels();
            Iterator<Level> i = levels.iterator();
            while (i.hasNext()) {
                Level level = i.next();
                if (level.getChests().size() == 0) {
                    i.remove();
                }
            }
        }
        Iterator<Cave> i = game.getCaves().iterator();
        while (i.hasNext()) {
            Cave cave = i.next();
            if (cave.getLevels().size() == 0) {
                i.remove();
            }
        }
    }

    private void putPlayersAtSurface(Game game) {
        for (Player player : game.getPlayers()) {
            player.setLevelIndex(null);
            player.setCaveIndex(null);
        }
    }

    private void calculateScore(Game game) {
        for (Player player : game.getPlayers()) {
            int newTreasures = 0;
            for (Chest chest : player.getChestsHolding()) {
                newTreasures += chest.getTreasureCount();
            }
            player.setTreasureCount(player.getTreasureCount() + newTreasures);
            player.setChestsHolding(new ArrayList<>());
        }
    }

    private void makeChestFolds(Game game) {
        for (Player player : game.getPlayers()) {
            if (player.getCaveIndex() != null && player.getLevelIndex() != null) {
                game.getLastLevel().getChests().addAll(player.getChestsHolding());
                player.setChestsHolding(new ArrayList<>());
            }
        }
    }


    public Player pickNextPlayer(List<Player> currentPlayers) {
        List<Player> playerAtLowestLevel = new ArrayList<>();
        Integer lowestLevel = null;
        Integer lowestCave = null;
        for (Player player : currentPlayers) {
            if (player.getCaveIndex() == lowestCave
                    && lowestLevel == player.getLevelIndex()) {
                playerAtLowestLevel.add(player);
            }
            if (player.getCaveIndex() != null && lowestCave == null ||
                    player.getCaveIndex() != null && lowestCave == null &&
                    player.getCaveIndex() > lowestCave ||
                    player.getCaveIndex() != null && lowestCave == null &&
                    player.getCaveIndex() == lowestCave &&
                    player.getLevelIndex() > lowestLevel) {
                playerAtLowestLevel = new ArrayList<>();
                playerAtLowestLevel.add(player);
                lowestLevel = player.getLevelIndex();
                lowestCave = player.getCaveIndex();
            }
        }
        Player player = playerAtLowestLevel.get((int) (Math.random() * playerAtLowestLevel.size()));
        currentPlayers.remove(player);
        return player;
    }

    public Stage(int oxygen) {
        this.oxygen = oxygen;
    }

    public int getOxygen() {
        return oxygen;
    }

    public void setOxygen(int oxygen) {
        this.oxygen = oxygen;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public void removeOxygen(int oxygen) {
        this.oxygen -= oxygen;
    }

}
