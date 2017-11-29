package com.grooptown.snorkunking.service.game.moves;

import com.grooptown.snorkunking.service.game.Chest;
import com.grooptown.snorkunking.service.game.Game;
import com.grooptown.snorkunking.service.game.Player;

/**
 * Created by thibautdebroca on 12/11/2017.
 */
public class PickTreasureMove implements Move {

    private int treasureIndex;

    public PickTreasureMove(int treasureIndex) {
        this.treasureIndex = treasureIndex;
    }

    @Override
    public boolean isValidMove(Game game, Player player) {
        System.out.println(game.getLevel(player));
        System.out.println(game.getLevel(player).getChests());
        return treasureIndex >= 0
                && treasureIndex < game.getLevel(player).getChests().size();
    }

    @Override
    public void playMove(Game game, Player player) {
        game.getCurrentStage().removeOxygen(1);
        Chest chest = game.getLevel(player).getChests().get(treasureIndex);
        player.addChest(chest);
        game.getLevel(player).getChests().remove(treasureIndex);
    }
}
