package server.engine;

import java.util.ArrayList;
import java.util.List;
import static server.engine.Settings.BJ_WIN_FACTOR;
import static server.engine.Settings.NORMAL_WIN_FACTOR;
import ws.blackjack.Event;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

public final class BetSolver {
    private final GameContext gameContext;
    private final List<Event> solverEvents;
    private int dealerHandValue;
    
    public BetSolver(GameContext gameContext, int dealerHandValue) {
        this.solverEvents = new ArrayList<>();
        this.gameContext = gameContext;
        this.dealerHandValue = dealerHandValue;
    }
    
    public void solve(Player player, Hand hand) {
        boolean playerHandIsBusted = hand.isBusted();
        boolean playerHandIsBlackJack = hand.isBlackJack();
        
        if (playerHandIsBlackJack) {
            solvePlayerBlackJack(player, hand);
        }
        
        else if (playerHandIsBusted) {
            solvePlayerIsBusted(player, hand);
        }
        
        else {
            solvePlayerHandNotBusted(player, hand);
        }
    }
    
    private void solvePlayerIsBusted(Player player, Hand hand) {
        boolean dealerIsBusted = dealerHandValue > 21;
        
        if (dealerIsBusted) { // get a refund
            player.setMoney(player.getMoney() + hand.getWager());
            Event event = getEventBuilder().createGameWinnerEvent(player.getName(), 0);
            solverEvents.add(event);
        }
        
        else { // dealer is not busted, player loses his money 
            Event event = getEventBuilder().createGameWinnerEvent(player.getName(), hand.getWager() * -1); // negative value for lost event
            solverEvents.add(event);
        }
    }

    private void solvePlayerBlackJack(Player player, Hand hand) {
        Event event = getEventBuilder().createGameWinnerEvent(player.getName(), hand.getWager() * BJ_WIN_FACTOR);
        solverEvents.add(event);
    }
    
    public List<Event> getResults() {
        return solverEvents;
    }
    
    private EventBuilder getEventBuilder() {
        return gameContext.getEventBuilder();
    }

    private void solvePlayerHandNotBusted(Player player, Hand hand) {
        boolean dealerIsBusted = dealerHandValue > 21;
        
        if (dealerIsBusted) { // get a refund
            player.setMoney(player.getMoney() + hand.getWager());
            Event event = getEventBuilder().createGameWinnerEvent(player.getName(), 0);
            solverEvents.add(event);
        }
        
        else {
            if (hand.getValue() > dealerHandValue) { // player wins
                player.setMoney(player.getMoney() + hand.getWager() * NORMAL_WIN_FACTOR);
                Event event = getEventBuilder().createGameWinnerEvent(player.getName(), hand.getWager() * NORMAL_WIN_FACTOR);
                solverEvents.add(event);
            }
            else { // dealer wins
                Event event = getEventBuilder().createGameWinnerEvent(player.getName(), hand.getWager() * -1);
                solverEvents.add(event);
            }
        }
    }
}
