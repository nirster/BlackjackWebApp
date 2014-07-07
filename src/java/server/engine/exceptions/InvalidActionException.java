package server.engine.exceptions;

import ws.blackjack.Action;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class InvalidActionException extends GameException {

    public InvalidActionException(Action action, String playerName) {
        super("Player " + playerName + " cannot perform action: " + action.toString());
    }
}
