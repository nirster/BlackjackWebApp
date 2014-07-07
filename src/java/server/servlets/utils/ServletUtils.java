package server.servlets.utils;

import java.security.InvalidParameterException;
import ws.blackjack.Action;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class ServletUtils {
    
    public static Action actionFromString(String value) throws InvalidParameterException {
        Action action;
        String normalized = value.toLowerCase();
        
        switch (normalized) {
            case "placebet":
                action = Action.PLACE_BET;
                break;
            case "hit":
                action = Action.HIT;
                break;
            case "double":
                action = Action.DOUBLE;
                break;
            case "stand":
                action = Action.STAND;
                break;
            case "split":
                action = Action.SPLIT;
                break;
            default:
                throw new InvalidParameterException(value + ": no such action.");
        }
        
        return action;
    }
}