package server.engine.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class InvalidEventIdException extends GameException {

    public InvalidEventIdException(int id) {
        super("Event id: " + id + "is not expected");
    }

}
