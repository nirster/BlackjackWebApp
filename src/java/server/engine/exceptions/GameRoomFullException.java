package server.engine.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class GameRoomFullException extends GameException {
    
    public GameRoomFullException(String roomName, int joinedPlayers, int requiredPlayers) {
        super("Game room: " + roomName + " already has " + joinedPlayers + " players out of " + requiredPlayers);
    }
}
