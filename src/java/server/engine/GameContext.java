package server.engine;

import server.engine.enums.GameMode;
import ws.blackjack.GameStatus;


/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class GameContext {
    private final EventBuilder eventBuilder;
    private final Deck deck;
    private GameMode gameMode;
    private GameStatus gameStatus;

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
    
    public GameContext() {
        eventBuilder = new EventBuilder();
        deck = new Deck();
        gameMode = GameMode.PLACING_BETS;
        gameStatus = GameStatus.WAITING;
    }
    
    public EventBuilder getEventBuilder() {
        return eventBuilder;
    }
    
    public Deck getDeck() {
        return deck;
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
}