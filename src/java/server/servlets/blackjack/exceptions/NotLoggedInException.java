package server.servlets.blackjack.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class NotLoggedInException extends RuntimeException {
    public NotLoggedInException() {
        super("You must be logged in.");
    }
}
