package server.engine;

import java.util.Timer;
import java.util.TimerTask;
import ws.blackjack.Event;

/**
 * Game event wrapper with timeout for PROMPT_USER_FOR_ACTION events
 * @author Nir Zarko <nirster@gmail.com>
 */

public class TimedEvent {
    private final Timer timer;
    private final TimerTask timerTask;
    private boolean wasResponded;
    private final Event event;

    public TimedEvent(final GameRoom gameRoom, final Event event, final int playerId) {
        this.event = event;
        wasResponded = false;
        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                if (!wasResponded) {
                    gameRoom.doResign(playerId);
                }
            }
        };
        
        timer.schedule(timerTask, 30 * 1000);
    }

    public void setResponded(boolean value) {
        wasResponded = value;
    }
    
    public void cancel() {
        timerTask.cancel();
        timer.cancel();
    }
    
    public String getName() {
        return event.getPlayerName();
    }
    
    public int getId() {
        return event.getId();
    }
}