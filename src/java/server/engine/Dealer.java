package server.engine;

import server.engine.enums.PlayerStatus;
import server.engine.enums.PlayerType;
import ws.blackjack.Event;

/**
 * Dealer implementation
 * @author Nir Zarko <nirster@gmail.com>
 */
public class Dealer extends Player {

    public Dealer(int playerId) {
        super("Dealer", 1000, playerId);
    }

    @Override
    public PlayerType getType() {
        return PlayerType.COMPUTER;
    }

    @Override
    public Event askForAction(GameContext gameContext) {
        Event event = null;
        
        switch (gameContext.getGameMode()) {
            case PLACING_BETS:
                event = drawInitialCard(gameContext);
                break;
            case MID_ROUND:
                event = dealerHitOrStand(gameContext);
                break;
        }
        
        return event;
    }

    private Event drawInitialCard(GameContext gameContext) {
        Hand hand = Hand.newHand(100);
        hands.add(hand);
        Card c = gameContext.getDeck().drawCard();
        hand.addCard(c);
        Event event = gameContext.getEventBuilder().createPlaceBetEvent("Dealer", 10); // TODO:
        setStatus(PlayerStatus.ACTIVE);
        
        return event;
    }

    private Event dealerHitOrStand(GameContext gameContext) {
        int handValue = getHands().get(Settings.FIRST_HAND).getValue();
        Hand hand = getHands().get(Settings.FIRST_HAND);

        if (handValue < 17) {
            // perform hit
            Card card = gameContext.getDeck().drawCard();
            hand.addCard(card);
            if (hand.isBusted()) {
                hand.setCanPlay(false);
            }

            if (hand.isBlackJack()) {
                hand.setCanPlay(false);
            }

            Event event = gameContext.getEventBuilder().createHitEvent(name, card);

            return event;

        } else {
            getHands().get(Settings.FIRST_HAND).setCanPlay(false);

            return gameContext.getEventBuilder().createStandEvent(name);
        }
    }
}
