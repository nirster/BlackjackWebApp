package server.servlets.blackjack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import server.servlets.utils.WSClient;
import ws.blackjack.BlackJackWebService;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public abstract class BlackjackServletBase extends HttpServlet {
    protected final class ServletContextAttrs {
        protected static final String WEB_SERVICE = "attribute_web_service";
    }
    protected final class AppUrls {
        protected static final String INDEX = "index.html";
        protected static final String GAME = "game.html";
    }
    protected final class RequestKeys {
        protected static final String LOAD_XML = "loadXml";
        protected static final String ROOM_NAME = "roomName";
        protected static final String PLAYER_MONEY = "playerMoney";
        protected static final String PLAYER_NAME = "playerName";
        protected static final String HUMAN_PLAYERS = "humanPlayers";
        protected static final String COMPUTER_PLAYERS = "computerPlayers";
        protected static final String GAME_ACTION = "gameAction";
        protected static final String LAST_EVENT_ID = "lastEventId";
        protected static final String RESPONSE_EVENT_ID = "responseEventId";
        protected static final String PLAYER_ACTION = "playerAction";
        protected static final String MONEY = "money";
        protected static final String BET = "bet";
    }

    protected final class RequestValues {
         protected static final String GET_SESSION_INFO = "getSessionInfo";
        protected static final String JOIN = "join";
        protected static final String PLACE_BET = "placebet";
        protected static final String HIT = "hit";
        protected static final String DOUBLE = "double";
        protected static final String STAND = "stand";
        protected static final String SPLIT = "split";
        protected static final String RESIGN = "resign";
        protected static final String GET_PLAYER_DETAILS = "getPlayerDetails";
        protected static final String GET_GAME_DETAILS = "getGameDetails";
        protected static final String GET_EVENTS = "getEvents";
        protected static final String GET_PLAYERS_DETAILS = "getPlayersDetails";
    }

    protected final class SessionAttrsKeys {
        protected static final String PLAYER_SESSION_DATA = "playerSessionData";
        protected static final String LAST_EVENT = "lastEventId";
        protected static final String PLAYER_ID = "playerId";
        protected static final String ROOM_NAME = "roomName";
        protected static final String PLAYER_NAME = "playerName";
    }

    protected BlackJackWebService getWebServiceEndPoint() {
        if (getServletContext().getAttribute(ServletContextAttrs.WEB_SERVICE) == null) {
            getServletContext().setAttribute(ServletContextAttrs.WEB_SERVICE, new WSClient(WSClient.OPEN_SHIFT_URL));
        }

            return ((WSClient) getServletContext().getAttribute(ServletContextAttrs.WEB_SERVICE)).getBlackJackPort();
    }

    protected String extractString(HttpServletRequest request, String key, String defaultValue) {
        return request.getParameter(key) != null ? request.getParameter(key) : defaultValue;
    }

    protected boolean isRequestContainsKeys(HttpServletRequest request, String... keys) {
        for (String key : keys) {
            if (request.getParameter(key) == null) {
                return false;
            }
        }

        return true;
    }

    protected int extractInt(HttpServletRequest request, String key, int defaultValue) {
        if (request.getParameter(key) != null) {
            try {
                int value = Integer.parseInt(request.getParameter(key));
                return value;
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    protected float extractFloat(HttpServletRequest request, String key, float defaultValue) {
        if (request.getParameter(key) != null) {
            try {
                float value = Float.parseFloat(request.getParameter(key));
                return value;
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    protected void sendJsonError(HttpServletResponse response, String message) {
        try {
            JsonResponse jsonResponse = new JsonResponse(true, message);
            String jsonMessage = new Gson().toJson(jsonResponse);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonMessage);
            response.getWriter().close();
        } catch (IOException ex) {
            Logger.getLogger(BlackjackServletBase.class.getName()).log(Level.SEVERE, message, ex);
        }
    }

    protected void sendGameLogicError(HttpServletResponse response, String message) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", Boolean.FALSE);
            jsonResponse.addProperty("message", message);
            response.getWriter().write(jsonResponse.toString());
            response.getWriter().close();
        } catch (IOException ex) {
            Logger.getLogger(BlackjackServletBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}