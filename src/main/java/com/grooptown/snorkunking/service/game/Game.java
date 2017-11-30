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

    private boolean isFinished;

    private int oxygen = 3;

    private int caveCount = 3;

    private List<Stage> stages = new ArrayList<>();

    private List<Player> players = new ArrayList<>();

    private List<Cave> caves = new ArrayList<>();

    int currentStageIndex = 0;

    private List<Player> leaderboard;

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

    public void startGame() {
        getCurrentStage().initStage(this);
        setStarted(true);
    }

    public void launch() {
        for (Stage stage: stages) {
            stage.playStage(this);
            afterStage();
        }
        endGame();
    }

    public void afterStage() {
        if (currentStageIndex == stages.size() - 1) {
            endGame();
        } else {
            currentStageIndex++;
            getCurrentStage().initStage(this);
        }
    }

    public void endGame() {
        isFinished = true;
        leaderboard = getLeaderBoard();
        displayResults(leaderboard);
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

    private String displayResults(List<Player> leaderBoard) {
        StringBuilder display = new StringBuilder();
        display.append("=================================\n");
        display.append("= The End =\n");
        display.append("=================================\n");
        display.append("The Winner is " + leaderBoard.get(0).getName() + " with " + leaderBoard.get(0).getTreasureCount() + " treasures\n");
        display.append("Second is " + leaderBoard.get(1).getName() + " with " + leaderBoard.get(1).getTreasureCount() + " treasures\n");
        for (int i = 2; i < leaderBoard.size(); i++) {
            display.append(i + "th is " + leaderBoard.get(i).getName() + " with " + leaderBoard.get(i).getTreasureCount() + " treasures\n");
        }
        return display.toString();
    }

    public String displayGame() {
        StringBuilder display = new StringBuilder();
        Stage currentStage = stages.get(currentStageIndex);
        display.append("=================================\n");
        display.append("= Stage " + (currentStageIndex + 1) + " - Turn " + currentStage.getTurn() + "  =\n");
        display.append("=================================\n");
        if (isFinished) {
            display.append(displayResults(getLeaderBoard()));
        }
        display.append("Oxygen is : " + currentStage.getOxygen() + "\n");
        for (Player player : players) {
            display.append(player.getName() + " has " + player.getTreasureCount() + " treasures and holding " + player.getChestsHolding().size() + " chests.\n");
        }

        display.append("\nOn surface, there is : " +
                (getPlayersAtSurface().size() == 0 ? "Nobody" : getPlayersAtSurface()) + "\n");

        for (int c = 0; c < caves.size(); c++) {
            Cave cave = caves.get(c);
            display.append("\nCave : " + cave.getName() + "\n");
            for (int l = 0; l < cave.getLevels().size(); l++) {
                display.append("Level " + (l+1) + " : " + cave.getLevels().get(l).getChests() + "");
                if (getPlayersInLevel(c, l).size() != 0) {
                    display.append(" There is : " + getPlayersInLevel(c, l) + "");
                }
                display.append("\n");
            }
        }

        return display.toString();
    }

    public String getAsString() {
        return displayGame();
    }

    public int getCurrentIdPlayerTurn() {
        Stage currentStage = getCurrentStage();
        return currentStage.getCurrentIdPlayerTurn();
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

    public List<Player> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<Player> leaderboard) {
        this.leaderboard = leaderboard;
    }


    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}