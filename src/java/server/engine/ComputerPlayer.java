package server.engine;

import server.engine.enums.PlayerType;
import server.engine.enums.PlayerStatus;
import static server.engine.Settings.BJ_WIN_FACTOR;
import static server.engine.Settings.COMPUTER_DRAW_THRESHOLD;
import static server.engine.Settings.FIRST_HAND;
import static server.engine.Settings.WAGE_VALUE_1;
import static server.engine.Settings.WAGE_VALUE_2;
import static server.engine.Settings.WAGE_VALUE_3;
import static server.engine.Settings.WAGE_VALUE_4;
import ws.blackjack.Event;

/**
 * Computer player implementation
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class ComputerPlayer extends Player {
    public ComputerPlayer(String name, float money, int playerId) {
        super(name, money, playerId);
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
                event = placeBet(gameContext);
                break;
            case MID_ROUND:
                event = doMove(gameContext);
                break;
        }

        return event;
    }

    private Event doMove(GameContext gameContext) {
        Event event;
        Hand hand = getHands().get(FIRST_HAND);

        if (hand.getValue() <= COMPUTER_DRAW_THRESHOLD) {
            event = doHit(hand, gameContext);
        } else {
            event = doStand(hand, gameContext);
        }

        return event;
    }

    private Event doHit(Hand hand, GameContext gameContext) {
        Card card = gameContext.getDeck().drawCard();
        hand.addCard(card);

        if (hand.isBlackJack()) {
            hand.setCanPlay(false);
            this.money += (hand.getWager() * BJ_WIN_FACTOR);
        }

        if (hand.isBusted()) {
            hand.setCanPlay(false);
        }

        Event event = gameContext.getEventBuilder().createHitEvent(this.name, card);

        return event;
    }

    private Event doStand(Hand hand, GameContext gameContext) {
        hand.setCanPlay(false);
        Event event = gameContext.getEventBuilder().createStandEvent(this.name);

        return event;
    }

    private Event placeBet(GameContext gameContext) {
        float wager = calculateWager();
        // kick the player if he has no money
        if (wager == 0) {
            setStatus(PlayerStatus.RETIRED);
            Event event = gameContext.getEventBuilder().createResignEvent(this.name);
            return event;
        }
        // otherwise:
        this.money -= wager;
        Hand hand = Hand.newHand(wager);
        this.hands.add(hand);
        setPlacedBet(true);
        Event event = gameContext.getEventBuilder().createPlaceBetEvent(this.name, wager);
        setStatus(PlayerStatus.ACTIVE);

        return event;
    }

    // get the highest possible wager
    private int calculateWager() {
        int[] values = {WAGE_VALUE_4, WAGE_VALUE_3, WAGE_VALUE_2, WAGE_VALUE_1};

        for (int wager : values) {
            if (this.money >= wager) {
                return wager;
            }
        }

        return 0;
    }
}