package server.engine;

import server.engine.enums.HandAction;
import server.engine.enums.PlayerType;
import server.engine.enums.PlayerStatus;
import java.util.ArrayList;
import java.util.List;
import server.engine.exceptions.InvalidActionException;
import server.engine.utils.Converter;
import ws.blackjack.Action;
import ws.blackjack.Event;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public abstract class Player {
    protected final String name;
    protected final int playerId;
    protected float money;
    protected List<Hand> hands;
    protected PlayerStatus status;
    protected boolean placedBet;
    
    public Player(String name, float money, int playerId) {
        this.name = name;
        this.money = money;
        this.status = PlayerStatus.JOINED;
        this.playerId = playerId;
        this.hands = new ArrayList<>();
    }
    
    public abstract PlayerType getType();
    
    public abstract Event askForAction(GameContext gameContext);
    
    public String getName() {
        return this.name;
    }
    
    public void initNewRound() {
        this.hands.clear();
        this.status = PlayerStatus.JOINED;
        this.placedBet = false;
    }
    
    public boolean isPlacedBet() {
        return placedBet;
    }
    
    public void setPlacedBet(boolean value) {
        placedBet = value;
    }
    
    public float getMoney() {
        return this.money;
    }
    
    public void setMoney(float value) {
        this.money = value;
    }
    
    public PlayerStatus getStatus() {
        return this.status;
    }
    
    public boolean canDoMoreMoves() {
        for (Hand hand : getHands()) {
            if (hand.canPlay())
                return true;
        }
        
        return false;
    }
    
    public void setStatus(PlayerStatus status) {
        this.status = status;
    }
    
    public int getId() {
        return this.playerId;
    }
    
    public List<Hand> getHands() {
        return this.hands;
    }
    
    public boolean canPlaceBet() {
        return this.money >= Settings.WAGE_VALUE_1;
    }
    
    public boolean canDoAction(Action action, float money, int bet) {
        if (bet != Settings.FIRST_HAND && bet != Settings.SECOND_HAND) {
            throw new InvalidActionException(action, name);
        }
        
        if (hands.get(bet) == null) {
            throw new InvalidActionException(action, name);
        }
        
        Hand hand = hands.get(bet);
        HandAction requestedHandAction = Converter.toHandAction(action);
        List<HandAction> actions = hand.getValidActions(this.money);
        for (HandAction handAction : actions) {
            if (requestedHandAction == handAction) {
                return true;
            }
        }
        
        return false;
    }
    
    public Event dealInitialCards(GameContext gameContext) {
        Hand hand = hands.get(Settings.FIRST_HAND);
        Card[] cards = {gameContext.getDeck().drawCard(), gameContext.getDeck().drawCard()};
        
        for (Card c : cards) {
            hand.addCard(c);
        }
        
        return gameContext.getEventBuilder().createCardsDealtEvent(name, cards);
    }
}
