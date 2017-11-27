package com.grooptown.snorkunking.service.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thibautdebroca on 11/11/2017.
 */
public class Level {

    private List<Chest> chests;

    public Level(int minTreasureCount, int maxTreasureCount) {
        chests = new ArrayList<>();
        chests.add(new Chest(minTreasureCount, maxTreasureCount));
    }

    public List<Chest> getChests() {
        return chests;
    }

    public void setChests(List<Chest> chests) {
        this.chests = chests;
    }
}
