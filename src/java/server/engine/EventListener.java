package server.engine;

import ws.blackjack.Event;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public interface EventListener {
    void newEvent(final Event event);
}
