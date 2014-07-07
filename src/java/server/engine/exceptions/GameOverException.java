package server.engine.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class GameOverException extends GameException {

    public GameOverException(String gameName) {
        super("Game " + gameName + " is over.");
    }

}
