package server.engine.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

public final class IDGenerator {
    private final AtomicInteger playerId = new AtomicInteger(1);
    private final AtomicInteger eventId = new AtomicInteger(0);
    
    public int getPlayerId() {
       return playerId.getAndIncrement();
    }
    
    public int getEventId() {
        return eventId.getAndIncrement();
    }
}