package server.engine.exceptions;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class EventNotFoundException extends GameException {
    public EventNotFoundException(int eventIdReceived, int lastEventId) {
        super("Got response to event: " + eventIdReceived + ". Expected: " + lastEventId);
    }
}
