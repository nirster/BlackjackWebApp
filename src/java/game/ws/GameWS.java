package game.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import server.engine.GameConfig;
import server.engine.GameOverListener;
import server.engine.GameRoom;
import server.engine.Player;
import server.engine.Settings;
import server.engine.XmlLoader;
import server.engine.exceptions.GameException;
import server.engine.exceptions.InvalidXMLPlayerName;
import server.engine.utils.Converter;
import ws.blackjack.Action;
import ws.blackjack.DuplicateGameName;
import ws.blackjack.DuplicateGameName_Exception;
import ws.blackjack.Event;
import ws.blackjack.GameDetails;
import ws.blackjack.GameDoesNotExists;
import ws.blackjack.GameDoesNotExists_Exception;
import ws.blackjack.GameStatus;
import ws.blackjack.InvalidParameters;
import ws.blackjack.InvalidParameters_Exception;
import ws.blackjack.InvalidXML;
import ws.blackjack.InvalidXML_Exception;
import ws.blackjack.PlayerDetails;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

@WebService(serviceName = "BlackJackWebService", 
        portName = "BlackJackWebServicePort", 
        endpointInterface = "ws.blackjack.BlackJackWebService", 
        targetNamespace = "http://blackjack.ws/", 
        wsdlLocation = "WEB-INF/wsdl/GameWS/BlackJackWebService.wsdl")
public class GameWS {
    private final ConcurrentMap<String, GameRoom> gameRooms = new ConcurrentHashMap<>();
    
    public List<Event> getEvents(int playerId, int eventId) throws InvalidParameters_Exception {
        GameRoom gameRoom = tryGetRoomByPlayerId(playerId);
        
        return gameRoom.getEvents(eventId);
    }

    public String createGameFromXML(String xmlData) throws InvalidParameters_Exception, DuplicateGameName_Exception, InvalidXML_Exception {
        GameConfig gameConfig = new GameConfig(xmlData, new XmlLoader());
        throwIfGameAlreadyExists(gameConfig.getName());
        
        GameRoom gameRoom = new GameRoom(gameConfig);
        gameRooms.putIfAbsent(gameRoom.getName(), gameRoom);
        gameRoom.setGameOverListener(new GameOverListener() {

            @Override
            public void gameOver(final String gameName) {
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {

                    @Override
                    public void run() {
                        gameRooms.remove(gameName);
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Room: {0} was deleted.", gameName);
                    }
                };
                
                timer.schedule(timerTask, 5 * 1000);
            }
        });
        
        return gameRoom.getName();
    }

    public GameDetails getGameDetails(String gameName) throws GameDoesNotExists_Exception {
        if (isEmptyOrNull(gameName)) {
            throw new GameDoesNotExists_Exception("cant provide empty Strings", new GameDoesNotExists());
        }
        
        GameRoom gameRoom = tryGetRoomByName(gameName);

        return createGameDetailsForGame(gameRoom);
    }

    public List<String> getWaitingGames() {
        return getGamesByStatus(GameStatus.WAITING);
    }

    public List<String> getActiveGames() {
        return getGamesByStatus(GameStatus.ACTIVE);
    }

    public List<PlayerDetails> getPlayersDetails(String gameName) throws GameDoesNotExists_Exception {
        if (isEmptyOrNull(gameName)) {
            throw new GameDoesNotExists_Exception("cant provide empty Strings", new GameDoesNotExists());
        }
        
        GameRoom gameRoom = tryGetRoomByName(gameName);
        List<PlayerDetails> playersDetails = new ArrayList<>();
        for (Player player : gameRoom.getPlayers()) {
            playersDetails.add(createPlayerDetailsForPlayer(player));
        }

        PlayerDetails dealerDetails = createPlayerDetailsForPlayer(gameRoom.getDealer());
        playersDetails.add(dealerDetails);
        
        return playersDetails;
    }

    public PlayerDetails getPlayerDetails(int playerId) throws InvalidParameters_Exception, GameDoesNotExists_Exception {
        GameRoom gameRoom = tryGetRoomByPlayerId(playerId);
        Player player = tryGetPlayerById(gameRoom, playerId);
        
        return createPlayerDetailsForPlayer(player);
    }

    public void createGame(String name, int humanPlayers, int computerizedPlayers) throws InvalidParameters_Exception, DuplicateGameName_Exception {
        if (isEmptyOrNull(name)) {
            throw new InvalidParameters_Exception("cant provide empty strings", new InvalidParameters());
        }
        
        final int playersCount = humanPlayers + computerizedPlayers;
        throwIfGameAlreadyExists(name);
        throwIfBadPlayersCount(playersCount, humanPlayers);
        
        GameRoom gameRoom = new GameRoom(name, humanPlayers, computerizedPlayers);
        gameRooms.putIfAbsent(name, gameRoom);
        gameRoom.setGameOverListener(new GameOverListener() {

            @Override
            public void gameOver(final String gameName) {
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {

                    @Override
                    public void run() {
                        gameRooms.remove(gameName);
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Room: {0} was deleted.", gameName);
                    }
                };
                
                timer.schedule(timerTask, 5 * 1000);
            }
        });
    }

    public int joinGame(String gameName, String playerName, float money) throws GameDoesNotExists_Exception, InvalidParameters_Exception {
        if (isEmptyOrNull(gameName, playerName)) {
            throw new GameDoesNotExists_Exception("cant provide empty Strings", new GameDoesNotExists());
        }
        
        GameRoom gameRoom = tryGetRoomByName(gameName);
        try {
            return gameRoom.joinPlayer(playerName, money);
        } catch (GameException e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }

    public void playerAction(int playerId, int eventId, Action action, float money, int bet) throws InvalidParameters_Exception {
        GameRoom gameRoom = tryGetRoomByPlayerId(playerId);
        throwIfInvalidBet(bet);
        try {
            gameRoom.doPlayerAction(playerId, eventId, action, money, bet);
        } catch (GameException e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }
    
    public void resign(int playerId) throws InvalidParameters_Exception {
        GameRoom gameRoom = tryGetRoomByPlayerId(playerId);
        
        gameRoom.doResign(playerId);
    }
    
    private GameRoom tryGetRoomByPlayerId(int playerId) throws InvalidParameters_Exception {
        for (GameRoom gameRoom : gameRooms.values()) {
            if (gameRoom.containsPlayer(playerId)) {
                return gameRoom;
            }
        }
        
        throw new InvalidParameters_Exception("Game not found for player with id: " + playerId, new InvalidParameters());
    }
    
    private PlayerDetails createPlayerDetailsForPlayer(Player player) {
        PlayerDetails playerDetails = new PlayerDetails();
        
        if (player.getHands().size() >= 1) {
            playerDetails.getFirstBet().addAll(Converter.toWsCards(player.getHands().get(0).getCards()));
            playerDetails.setFirstBetWage(player.getHands().get(0).getWager());
        }
        
        if (player.getHands().size() >= 2) {
            playerDetails.getSecondBet().addAll(Converter.toWsCards(player.getHands().get(1).getCards()));
            playerDetails.setSecondBetWage(player.getHands().get(1).getWager());
        }
        
        playerDetails.setMoney(player.getMoney());
        playerDetails.setName(player.getName());
        playerDetails.setType(Converter.toWsPlayerType(player.getType()));
        playerDetails.setStatus(Converter.toWsPlayerStatus(player.getStatus()));
        
        return playerDetails;
    }
    
    private List<String> getGamesByStatus(GameStatus gameStatus) {
        List<String> games = new ArrayList<>();
        
        for (GameRoom gameRoom : gameRooms.values()) {
            if (gameRoom.getStatus() == gameStatus) {
                games.add(gameRoom.getName());
            }
        }
        
        return games;
    }
    private GameDetails createGameDetailsForGame(GameRoom gameRoom) {
        GameDetails gameDetails = new GameDetails();
        
        gameDetails.setComputerizedPlayers(gameRoom.getComputerPlayers());
        gameDetails.setHumanPlayers(gameRoom.getHumanPlayers());
        gameDetails.setJoinedHumanPlayers(gameRoom.getJoinedHumanPlayers());
        gameDetails.setLoadedFromXML(gameRoom.isLoadedFromXML());
        gameDetails.setName(gameRoom.getName());
        gameDetails.setStatus(gameRoom.getStatus());
        gameDetails.setMoney(gameRoom.getMoney());
        
        return gameDetails;
    }
    
    private GameRoom tryGetRoomByName(String gameName) throws GameDoesNotExists_Exception {
        if (this.gameRooms.containsKey(gameName)) {
            return this.gameRooms.get(gameName);
        } else {
            throw new GameDoesNotExists_Exception("Can't find game with name: " + gameName, new GameDoesNotExists());
        }
    }
    
    private Player tryGetPlayerById(GameRoom gameRoom, int playerId) throws InvalidParameters_Exception {
        List<Player> players = gameRoom.getPlayers();
        
        for (Player player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        
        throw new InvalidParameters_Exception("Player with id: " + playerId + " not found", new InvalidParameters());
    }
    
    private boolean isEmptyOrNull(String... params) {
        for (String str : params) {
            if (str == null) {
                return true;
            }
            
            if (str.isEmpty()) {
                return true;
            }
        }

        return false;
    }
    
    private void throwIfGameAlreadyExists(String name) throws DuplicateGameName_Exception {
        if (gameRooms.containsKey(name)) {
            throw new DuplicateGameName_Exception("There is already a game with name " + name, new DuplicateGameName());
        }
    }
    
    private void throwIfBadPlayersCount(final int playersCount, int humanPlayers) throws InvalidParameters_Exception {
        if (playersCount > 6 || playersCount < 1) {
            throw new InvalidParameters_Exception("players count must be between 1 and 6", new InvalidParameters());
        }

        if (humanPlayers < 1) {
            throw new InvalidParameters_Exception("must be at least 1 human player", new InvalidParameters());
        }
    }
    
    private void throwIfInvalidBet(int bet) throws InvalidParameters_Exception {
        if (bet != Settings.FIRST_HAND && bet != Settings.SECOND_HAND)
            throw new InvalidParameters_Exception("Bet must be 0 or 1", new InvalidParameters());
    }
}