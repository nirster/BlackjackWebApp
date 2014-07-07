package server.engine.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class GameAlreadyStartedException extends GameException {
    
    public GameAlreadyStartedException(String roomName) {
        super("The game: " + roomName + " is already active.");
    }

}
