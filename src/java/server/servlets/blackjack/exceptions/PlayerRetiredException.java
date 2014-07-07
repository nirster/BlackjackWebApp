package server.servlets.blackjack.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class PlayerRetiredException extends RuntimeException {
    public PlayerRetiredException(String playerName) {
        super(playerName + " is retired.");
    }
}
