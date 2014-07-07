package server.engine.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class PlayerNotFoundException extends GameException {
    
    public PlayerNotFoundException(int playerID, String roomName) {
        super("player with id: " + playerID + " not found in game room: " + roomName);
    }
    
    public PlayerNotFoundException(String playerName, String roomName) {
        super("player with name: " + playerName + " not found in game room: " + roomName);
    }
}
