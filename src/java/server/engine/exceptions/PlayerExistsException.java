package server.engine.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class PlayerExistsException extends GameException {
    
    public PlayerExistsException(String playerName, String roomName) {
        super("Player with name: " + playerName + " already exists in game room: " + roomName);
    }
}
