package com.grooptown.snorkunking.service.game.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grooptown.snorkunking.service.game.PlayerInstance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import  com.grooptown.snorkunking.service.game.Game;

public class PlayerConnector {

	private int gameId;

    private String baseUrl = "http://localhost:8080";

    private String playerUUID;

    private PlayerInstance playerInstance;

    public PlayerConnector(int gameId, String baseUrl) {
        this.setGameId(gameId);
        this.baseUrl = baseUrl;
    }

	public PlayerConnector(int gameId, String baseUrl, String playerName) {
		this.setGameId(gameId);
        this.baseUrl = baseUrl;
        registerPlayer(playerName);
	}

	public static void main(String[] args) throws Exception {
        PlayerConnector c = new PlayerConnector(4, "http://localhost:8080", "Bob");
		System.out.println(c.getGame());
	}

    public static int addGame(String baseUrl, int gridSize) {
        return (Integer) sentGetAndDeserialize(baseUrl + "/api/iaconnector/addGame?gridSize=" + gridSize, Integer.class);
    }

    public void registerPlayer(String playerName) {
        try {
            playerInstance = (PlayerInstance)
                sentGetAndDeserialize(
                    baseUrl + "/api/iaconnector/addPlayer?idGame=" + gameId + "&playerName=" + playerName,
                    PlayerInstance.class);
            playerUUID = playerInstance.getUUID();
            System.out.println("Player has been registered. Player UUID is " + playerUUID);
        } catch (Exception e) {
            System.err.println("There was an error while registering player.");
            e.printStackTrace();
        }
    }

    public Game getGame() {
        return (Game) sentGetAndDeserialize(baseUrl + "/api/iaconnector/game?idGame=" + gameId, Game.class);
    }

    public boolean sendMove(char c) {
        if (playerUUID == null) {
            System.err.println("you have not registered to the game. Please call registerPlayer()");
            return false;
        }
        MessageResponse m = (MessageResponse)sentGetAndDeserialize(
            baseUrl + "/api/iaconnector/sendMove?playerUUID=" + playerUUID + "&color=" + c, MessageResponse.class);
        if (m.getError() != null) {
            System.err.println("Error with the move: " + m.getError());
            return false;
        } else {
            System.out.println("Move '"  + c + "' has been played !");
            return true;
        }
    }

    public Game waitOppenentsAndGetTheirMoves() {
        return ((Game) sentGetAndDeserialize(baseUrl + "/api/iaconnector/getOpponentMoves?playerUUID=" + playerUUID, Game.class));
    }


	private static Object sentGetAndDeserialize(String url, Class className) {
		try {
			String jsonInString = sendGet(url);
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(jsonInString, className);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


	private static String sendGet(String url) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();

	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

    public PlayerInstance getPlayerInstance() {
        return playerInstance;
    }

    public void setPlayerInstance(PlayerInstance playerInstance) {
        this.playerInstance = playerInstance;
    }
}
