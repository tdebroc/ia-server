package com.grooptown.snorkunking.service.game;

/**
 * Created by thibautdebroca on 11/11/2017.
 */
public class Chest {
    private String name;

    private int treasureCount;

    public Chest(int minTreasureCount, int maxTreasureCount) {
        treasureCount = (int) (Math.random() * (maxTreasureCount - minTreasureCount)) + minTreasureCount;
        name = "Chest with " + minTreasureCount + " to " + maxTreasureCount + " treasures";
    }

    @Override
    public String toString() {
        return "Chest{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTreasureCount() {
        return treasureCount;
    }

    public void setTreasureCount(int treasureCount) {
        this.treasureCount = treasureCount;
    }
}
