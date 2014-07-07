package server.engine;

import java.util.ArrayList;
import java.util.List;
import ws.blackjack.Event;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class EventManager {
    private final List<Event> events;
    private final List<EventListener> callbacks;
    
    public EventManager() {
        events = new ArrayList<>();
        callbacks = new ArrayList<>();
    }
    
    public void addEventListener(EventListener e) {
        callbacks.add(e);
    }
    
    public void removeEventListener(EventListener e) {
        callbacks.remove(e);
    }
    
    public synchronized List<Event> getEvents() {
        return this.events;
    }
    
    public boolean hasEvents() {
        return !events.isEmpty();
    }
    
    public Event getLastEvent() {
        return events.get(events.size() - 1);
    }
    
    public synchronized void addEvent(Event event) {
        events.add(event);
        for (EventListener callback : callbacks) {
            if (callback != null) {
                callback.newEvent(event);
            }
        }
    }
}
