package server.engine;

import server.engine.enums.HandAction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class Hand {
    private final List<Card> cards;
    private float wager;
    private boolean isSplitted;
    private boolean canPlay = true;
    
    private Hand(float betWager, boolean isSplitted) {
        this.cards = new ArrayList<>();
        this.wager = betWager;
        this.isSplitted = isSplitted;
    }
    
    public static Hand newHand(float betWager) {
        Hand retHand = new Hand(betWager, false);
        return retHand;
    }
    
    public static Hand splitFrom(Hand otherHand) {
        Hand retHand = new Hand(otherHand.wager, true);
        otherHand.isSplitted = true;
        retHand.cards.add(otherHand.cards.get(1));
        otherHand.cards.remove(1);
        return retHand;
    }
    
    public List<Card> getCards() {
        return this.cards;
    }
    
    public float getWager() {
        return this.wager;
    }
    
    public void setWager(float wager) {
        this.wager = wager;
    }
    
    public int getValue() {
        int sum = 0;
        int acesCount = 0;

        for (Card c : cards) {
            if (c.getRank() == Card.Rank.ACE) {
                acesCount++;
            }
            sum += c.getRank().numericValue();
        }
        // Count aces as one if busted.
        while (sum > 21 && acesCount > 0) {
            acesCount--;
            sum -= 10;
        }
        return sum;
    }
    
    public boolean isBusted() {
        return getValue() > 21;
    }
    
    public boolean isBlackJack() {
        return  getValue() == 21 && 
                isSplitted == false &&
                cards.size() == 2;
    }
    
    public boolean isSplittable() {
        if (cards.size() != 2) {
            return false;
        }
        if (isSplitted) {
            return false;
        }
        return cards.get(0).getRank().numericValue() == cards.get(1).getRank().numericValue();
    }
    
    public List<HandAction> getValidActions(float playerFunds) {
        List<HandAction> validActions = new ArrayList<>();
        if (!isBusted()) {
            validActions.add(HandAction.HIT);
            validActions.add(HandAction.STAND);
            
            if (playerFunds >= this.wager)
                validActions.add(HandAction.DOUBLE);

            if (this.isSplittable() && playerFunds >= this.wager) {
                validActions.add(HandAction.SPLIT);
            }
        }
        return validActions;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card c : cards) {
            sb
            .append(c.toString())
            .append("\n");
        }
        sb.append("Value: ").append(getValue());
        return sb.toString();
    }
    
    public void addCard(Card card) {
        cards.add(card);
    }

    public void setCanPlay(boolean value) {
        canPlay = value;
    }
    
    public boolean canPlay() {
        return canPlay;
    }
    
    public boolean isGreaterThan(Hand otherHand) {
        return this.getValue() > otherHand.getValue();
    }
    
    public boolean isEqual(Hand otherHand) {
        return this.getValue() == otherHand.getValue();
    }
}