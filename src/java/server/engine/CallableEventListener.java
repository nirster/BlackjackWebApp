package server.engine;

import java.util.concurrent.Callable;
import ws.blackjack.Event;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class CallableEventListener implements EventListener, Callable<Boolean> {
    private Boolean result = false;
    private final Event promptEvent;

    public CallableEventListener(final Event event) {
        this.promptEvent = event;
    }
    
    @Override
    public void newEvent(final Event event) {
        if (event.getId() == promptEvent.getId()) {
            result = true;
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Boolean call() throws Exception {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                return result;
            }
        }
    }
}