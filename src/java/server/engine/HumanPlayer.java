package server.engine;

import static server.engine.Settings.BJ_WIN_FACTOR;
import static server.engine.Settings.FIRST_HAND;
import static server.engine.Settings.SECOND_HAND;
import server.engine.enums.PlayerStatus;
import server.engine.enums.PlayerType;
import server.engine.exceptions.InvalidActionException;
import ws.blackjack.Action;
import ws.blackjack.Event;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class HumanPlayer extends Player {
    
    public HumanPlayer(String name, float money, int playerId) {
        super(name, money, playerId);
    }

    @Override
    public PlayerType getType() {
        return PlayerType.HUMAN;
    }

    @Override
    public Event askForAction(GameContext gameContext) {
        return gameContext.getEventBuilder().createPromptForActionEvent(this.name);
    }
    
    public Event doAction(Action action, float money, int bet, GameContext gameContext) throws InvalidActionException {
        Event event = null;
        
        switch(action) {
            case PLACE_BET:
                event = placeBet(money, gameContext);
                break;
            case HIT:
                event = doHit(bet, gameContext);
                break;
            case DOUBLE:
                event = doDouble(bet, gameContext);
                break;
            case STAND:
                event = doStand(bet, gameContext);
                break;
            case SPLIT:
                event = doSplit(gameContext);
                break;
        }
        
        return event;
    }

    private Event placeBet(float money, GameContext gameContext) throws InvalidActionException {
        if (money <= 0) {
            throw new InvalidActionException(Action.PLACE_BET, name);
        }
        
        setPlacedBet(true);
        setStatus(PlayerStatus.ACTIVE);
        this.money -= money;
        Hand hand = Hand.newHand(money);
        hands.add(hand);
        Event event = gameContext.getEventBuilder().createPlaceBetEvent(name, money);
        
        return event;
    }

    private Event doHit(int bet, GameContext gameContext) throws InvalidActionException {
        if (bet != FIRST_HAND && bet != SECOND_HAND) {
            throw new InvalidActionException(Action.HIT, name);
        }
        
        Card card = gameContext.getDeck().drawCard();
        Hand hand = hands.get(bet);
        hand.addCard(card);
        
        if (hand.isBlackJack()) {
            hand.setCanPlay(false);
            money += hand.getWager() * BJ_WIN_FACTOR;
        }
        
        if (hand.isBusted()) {
            hand.setCanPlay(false);
        }
        
        Event event = gameContext.getEventBuilder().createHitEvent(name, card);
        
        return event;
    }

    private Event doDouble(int bet, GameContext gameContext) throws InvalidActionException {
        if (bet != FIRST_HAND && bet != SECOND_HAND) {
            throw new InvalidActionException(Action.HIT, name);
        }
        
        Card card = gameContext.getDeck().drawCard();
        Hand hand = hands.get(bet);
        hand.addCard(card);
        float currentWager = hand.getWager();
        float newWager = currentWager * Settings.NORMAL_WIN_FACTOR;
        hand.setWager(newWager);
        
        if (hand.isBlackJack()) {
            hand.setCanPlay(false);
            money += hand.getWager() * BJ_WIN_FACTOR;
        }
        
        if (hand.isBusted()) {
            hand.setCanPlay(false);
        }
        
        Event event = gameContext.getEventBuilder().createDoubleEvent(name, card);
        
        return event;
    }

    private Event doStand(int bet, GameContext gameContext) throws InvalidActionException {
        if (bet != FIRST_HAND && bet != SECOND_HAND) {
            throw new InvalidActionException(Action.HIT, name);
        }
        
        Hand hand = hands.get(bet);
        hand.setCanPlay(false);
        Event event = gameContext.getEventBuilder().createStandEvent(name);
        
        return event;
    }

    private Event doSplit(GameContext gameContext) {
        Hand firstHand = hands.get(Settings.FIRST_HAND);
        Hand newHand = Hand.splitFrom(firstHand);
        hands.add(newHand);
        money -= firstHand.getWager();
        Event event = gameContext.getEventBuilder().createSplitEvent(name, firstHand, newHand);
        
        return event;
    }
}