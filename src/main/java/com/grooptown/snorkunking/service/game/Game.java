package com.grooptown.snorkunking.service.game;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by thibautdebroca on 11/11/2017.
 */
public class Game {

    public static int MAX_NUM_PLAYER = 10;

    private int idGame;

    private boolean isStarted;

    private int oxygen = 3;

    private int caveCount = 3;

    private List<Stage> stages = new ArrayList<>();

    private List<Player> players = new ArrayList<>();

    private List<Cave> caves = new ArrayList<>();

    int currentStageIndex = 0;

    public Game(int oxygenFactor, int caveCount) {
        this.caveCount = caveCount;
        int maxLevel = (caveCount + 1) * caveCount;
        int treasureCount = 1;
        for (int i = 0; i < caveCount; i++) {
            String name = "Cave " + (i + 1);
            int levelCountMin = maxLevel - (i + 1) * 3;
            int levelCountMax = maxLevel - (i)* 3;
            int minTreasureCount = treasureCount;
            treasureCount += 3;
            int maxTreasureCount = treasureCount;
            caves.add(new Cave(name, levelCountMin, levelCountMax, minTreasureCount, maxTreasureCount));
            treasureCount += 2;
        }
        int oxygen = countLevels() * oxygenFactor;

        for (int i = 0; i < caveCount; i++) {
            stages.add(new Stage(oxygen));
        }

    }

    public void addPlayer(String playerName) {
        players.add(new Player(playerName));
    }

    public int countLevels() {
        int levelCount = 0;
        for (int i = 0; i < caves.size(); i++) {
            levelCount += caves.get(i).getLevels().size();
        }
        return levelCount;
    }

    public void launch() {
        for (Stage stage: stages) {
            stage.playStage(this);
            currentStageIndex++;
        }
        List<Player> players = getLeaderBoard();
        displayResults(players);
    }

    public List<Player> getLeaderBoard() {
        List<Player> leaderBoard = new ArrayList<>();
        leaderBoard.addAll(players);
        leaderBoard.sort(new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                return p1.getTreasureCount() > p2.getTreasureCount() ? -1 :
                        (p1.getTreasureCount() < p2.getTreasureCount()) ? 1 : 0;
            }
        });
        return leaderBoard;
    }

    private void displayResults(List<Player> leaderBoard) {
        System.out.println("=================================");
        System.out.println("= The End =");
        System.out.println("=================================");
        System.out.println("The Winner is " + leaderBoard.get(0).getName() + " with " + leaderBoard.get(0).getTreasureCount() + " treasures");
        System.out.println("Second is " + leaderBoard.get(1).getName() + " with " + leaderBoard.get(1).getTreasureCount() + " treasures");
        for (int i = 2; i < leaderBoard.size(); i++) {
            System.out.println(i + "th is " + leaderBoard.get(i).getName() + " with " + leaderBoard.get(i).getTreasureCount() + " treasures");
        }
    }

    public void displayGame() {
        Stage currentStage = stages.get(currentStageIndex);
        System.out.println("=================================");
        System.out.println("= Stage " + (currentStageIndex + 1) + " - Turn " + currentStage.getTurn() + "  =");
        System.out.println("=================================");
        System.out.println("Oxygen is : " + currentStage.getOxygen());
        for (Player player : players) {
            System.out.println(player.getName() + " has " + player.getTreasureCount() + " treasures and holding " + player.getChestsHolding().size() + " chests.");
        }

        System.out.println("\nOn surface, there is : " +
                (getPlayersAtSurface().size() == 0 ? "Nobody" : getPlayersAtSurface()));

        for (int c = 0; c < caves.size(); c++) {
            Cave cave = caves.get(c);
            System.out.println("\nCave : " + cave.getName());
            for (int l = 0; l < cave.getLevels().size(); l++) {
                System.out.print("Level " + (l+1) + " : " + cave.getLevels().get(l).getChests());
                if (getPlayersInLevel(c, l).size() != 0) {
                    System.out.print(" There is : " + getPlayersInLevel(c, l));
                }
                System.out.println("");
            }
        }
    }

    public List<Player> getPlayersAtSurface() {
        return getPlayersInLevel(null, null);
    }

    public List<Player> getPlayersInLevel(Integer caveIndex, Integer levelIndex) {
        List<Player> playersInLevel = new ArrayList<>();
        for (Player player : this.players) {
            if (player.getCaveIndex() == caveIndex && player.getLevelIndex() == levelIndex) {
                playersInLevel.add(player);
            }
        }
        return playersInLevel;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Cave> getCaves() {
        return caves;
    }

    public void setCaves(List<Cave> caves) {
        this.caves = caves;
    }

    public Integer getLastCaveIndex() {
        return getCaves().size() - 1;
    }

    public Level getLevel(Player player) {
        return caves.get(player.getCaveIndex()).getLevels().get(player.getLevelIndex());
    }

    public Stage getCurrentStage() {
        return stages.get(currentStageIndex);
    }

    public Level getLastLevel() {
        List<Level> lastLevels = caves.get(getLastCaveIndex()).getLevels();
        return lastLevels.get(lastLevels.size() - 1);
    }


    public int getIdGame() {
        return idGame;
    }

    public void setIdGame(int idGame) {
        this.idGame = idGame;
    }


    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }
}
