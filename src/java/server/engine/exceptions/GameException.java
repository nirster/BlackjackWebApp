package server.engine.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
/* Base exception other users of the BlackJack engine should be prepared to catch */
public abstract class GameException extends RuntimeException {
    public GameException(String message) {
        super(message);
    }
}