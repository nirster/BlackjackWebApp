package server.servlets.blackjack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import server.servlets.blackjack.exceptions.NotLoggedInException;
import server.servlets.blackjack.exceptions.PlayerRetiredException;
import ws.blackjack.Action;
import ws.blackjack.Event;
import ws.blackjack.GameDetails;
import ws.blackjack.GameDoesNotExists_Exception;
import ws.blackjack.GameStatus;
import ws.blackjack.InvalidParameters_Exception;
import ws.blackjack.PlayerDetails;
import ws.blackjack.PlayerStatus;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

@WebServlet(name = "GameServlet", urlPatterns = {"/game"})
public final class GameServlet extends BlackjackServletBase {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Logger.getLogger(getClass().getName()).log(Level.INFO, createRequestInfo(request));
        response.setContentType("application/json ;charset=UTF-8");
        try {
            throwIfNoSessionDataExists(request);
            throwIfPlayerIsRetired(request, response);
            if (isGameOver(request)) {
                invalidateSession(request);
                return;
            }
            if (isRequestContainsKeys(request, RequestKeys.GAME_ACTION)) {
                processGameActionRequest(request, response);
            }
            if (isRequestContainsKeys(request, RequestKeys.PLAYER_ACTION)) {
                processPlayerActionRequest(request, response);
            }
        } catch (NotLoggedInException | PlayerRetiredException | GameDoesNotExists_Exception e) {
            invalidateSession(request);
            sendGameLogicError(response, e.getMessage());
        }
    }

    private void processGameActionRequest(HttpServletRequest request, HttpServletResponse response) {
        String gameActionValue = extractString(request, RequestKeys.GAME_ACTION, "");
        
        switch (gameActionValue) {
            case RequestValues.GET_SESSION_INFO:
                sendSessionInfoToClient(request, response);
                break;
            case RequestValues.GET_EVENTS:
                wsGetEvents(request, response);
                break;
            case RequestValues.RESIGN:
                wsResign(request, response);
                break;
            case RequestValues.GET_PLAYERS_DETAILS:
                wsGetPlayersDetails(request, response);
                break;
            case RequestValues.GET_PLAYER_DETAILS:
                wsGetPlayerDetails(request, response);
                break;
            case RequestValues.GET_GAME_DETAILS:
                wsGetGameDetails(request, response);
                break;
        }
    }
    // This is just for debugging purposes.
    private void sendSessionInfoToClient(HttpServletRequest request, HttpServletResponse response) {
        try {
            String sessionInfoJson = new Gson().toJson((PlayerSessionData)request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA));
            response.getWriter().write(sessionInfoJson);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException ex) {
            Logger.getLogger(GameServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processPlayerActionRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            String playerActionValue = extractString(request, RequestKeys.PLAYER_ACTION, "");
            PlayerSessionData playerData = (PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA);
            int money = extractInt(request, RequestKeys.MONEY, 0);
            int bet = extractInt(request, RequestKeys.BET, -1);
            int responseId = extractInt(request, RequestKeys.RESPONSE_EVENT_ID, 0);
            
            switch (playerActionValue) {
                case RequestValues.PLACE_BET:
                    wsPlaceBet(playerData.getPlayerId(), responseId, money);
                    break;
                case RequestValues.HIT:
                    wsHit(playerData.getPlayerId(), responseId, bet);
                    break;
                case RequestValues.DOUBLE:
                    wsDouble(playerData.getPlayerId(), responseId, bet);
                    break;
                case RequestValues.SPLIT:
                    wsSplit(playerData.getPlayerId(), responseId);
                    break;
                case RequestValues.STAND:
                    wsStand(playerData.getPlayerId(), responseId, bet);
                    break;
            }
            
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", Boolean.TRUE);
            response.getWriter().write(jsonResponse.toString());
            response.getWriter().close();
        } catch (InvalidParameterException | InvalidParameters_Exception e) {
            sendGameLogicError(response, e.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(GameServlet.class.getName()).log(Level.SEVERE, null, ex);
            sendJsonError(response, "error processing request.");
        }
    }

    private void wsGetPlayerDetails(HttpServletRequest request, HttpServletResponse response) {
        try {
            PlayerSessionData playerData = (PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA);
            PlayerDetails playerDetails = getWebServiceEndPoint().getPlayerDetails(playerData.getPlayerId());
            String jsonPlayerDetails = new Gson().toJson(playerDetails);
            response.getWriter().write(jsonPlayerDetails);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (GameDoesNotExists_Exception | InvalidParameters_Exception ex) {
            sendGameLogicError(response, ex.getMessage());
        } catch (IOException ex) {
            sendJsonError(response, "error processing request.");
        }
    }

    private void wsGetGameDetails(HttpServletRequest request, HttpServletResponse response) {
        try {
            PlayerSessionData playerData = (PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA);
            GameDetails gameDetails = getWebServiceEndPoint().getGameDetails(playerData.getRoomName());
            String jsonGameDetails = new Gson().toJson(gameDetails);
            response.getWriter().write(jsonGameDetails);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (GameDoesNotExists_Exception ex) {
            sendGameLogicError(response, ex.getMessage());
        } catch (IOException ex) {
            sendJsonError(response, "error processing request.");
        }
    }

    private void wsPlaceBet(int playerId, int responseId, int money) throws InvalidParameters_Exception {
        getWebServiceEndPoint().playerAction(playerId, responseId, Action.PLACE_BET, money, 0);
    }

    private void wsHit(int playerId, int responseId, int bet) throws InvalidParameters_Exception {
        getWebServiceEndPoint().playerAction(playerId, responseId, Action.HIT, 0, bet);
    }

    private void wsDouble(int playerId, int responseId, int bet) throws InvalidParameters_Exception {
        getWebServiceEndPoint().playerAction(playerId, responseId, Action.DOUBLE, 0, bet);
    }

    private void wsSplit(int playerId, int responseId) throws InvalidParameters_Exception {
        getWebServiceEndPoint().playerAction(playerId, responseId, Action.SPLIT, 0, 0);
    }

    private void wsStand(int playerId, int responseId, int bet) throws InvalidParameters_Exception {
        getWebServiceEndPoint().playerAction(playerId, responseId, Action.STAND, 0, bet);
    }

    private void wsGetEvents(HttpServletRequest request, HttpServletResponse response) {
        try {
            PlayerSessionData playerData = (PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA);
            List<Event> events;
            events = getWebServiceEndPoint().getEvents(playerData.getPlayerId(), playerData.getLastEventId());
            Type listType = new TypeToken<ArrayList<Event>>() {}.getType();
            String jsonEvents = new Gson().toJson(events, listType);
            
            if (!events.isEmpty()) {
                playerData.setLastEventId(events.get(events.size() - 1).getId());
                request.getSession().setAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA, playerData);
            }
            
            response.getWriter().write(jsonEvents);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (InvalidParameters_Exception ex) {
            sendGameLogicError(response, ex.getMessage());
        } catch (IOException ex) {
            sendJsonError(response, "error processing request.");
        }
    }

    private void wsResign(HttpServletRequest request, HttpServletResponse response) {
        try {
            PlayerSessionData playerData = (PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA);
            getWebServiceEndPoint().resign(playerData.getPlayerId());
            request.getSession().invalidate();
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", Boolean.TRUE);
            response.getWriter().write(jsonResponse.toString());
            response.getWriter().close();
        } catch (InvalidParameters_Exception ex) {
            sendGameLogicError(response, ex.getMessage());
        } catch (IOException ex) {
            sendJsonError(response, "error processing request.");
        }
    }

    private void wsGetPlayersDetails(HttpServletRequest request, HttpServletResponse response) {
        try {
            PlayerSessionData playerData = (PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA);
            List<PlayerDetails> playerDetailsList = getWebServiceEndPoint().getPlayersDetails(playerData.getRoomName());
            Type playerDetailsListType = new TypeToken<ArrayList<PlayerDetails>>() {}.getType();
            String jsonPlayerDetailsList = new Gson().toJson(playerDetailsList, playerDetailsListType);
            response.getWriter().write(jsonPlayerDetailsList);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (GameDoesNotExists_Exception e) {
            sendGameLogicError(response, e.getMessage());
        } catch (IOException ex) {
            sendJsonError(response, "error processing request.");
        }
    }
    
    private boolean isRequestHasPlayerSessionData(HttpServletRequest request) {
        return request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA) != null;
    }
    // Used for debugging.
    private String createRequestInfo(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request Info start\n");
        if (request.getSession(false) != null) {
            sb.append("SessionID: ").append(request.getSession(false).getId()).append("\n");
            
            if (request.getSession(false).getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA) != null) {
                PlayerSessionData psd = (PlayerSessionData) request.getSession(false).getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA);
                sb.append("PlayerSessionData:\n");
                sb.append("\t").append("playerId: ").append(psd.getPlayerId()).append("\n");
                sb.append("\t").append("lastEventId: ").append(psd.getLastEventId()).append("\n");
                sb.append("\t").append("roomName: ").append(psd.getRoomName()).append("\n");
                sb.append("\t").append("playerName: ").append(psd.getPlayerName()).append("\n");
            } else {
                sb.append("No PlayerSessionData found\n");
            }
        }
        
        else {
            sb.append("No Session found\n");
        }
        
        sb.append("Request parameters:\n");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            sb.append("\t").append(paramName).append(" : ");
            String[] paramValues = request.getParameterValues(paramName);
            for (String paramValue : paramValues) {
                sb.append(paramValue);
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST); // Accept only POST.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Game Servlet";
    }

    private void throwIfNoSessionDataExists(HttpServletRequest request) throws NotLoggedInException {
        if (!isRequestHasPlayerSessionData(request)) {
            throw new NotLoggedInException();
        }
    }

    private void throwIfPlayerIsRetired(HttpServletRequest request, HttpServletResponse response) throws PlayerRetiredException {
        int playerId = ((PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA)).getPlayerId();
        
        try {
            PlayerStatus ps = getWebServiceEndPoint().getPlayerDetails(playerId).getStatus();
            if (ps == PlayerStatus.RETIRED) {
                if (isRequestHasPlayerSessionData(request)) {
                    throw new PlayerRetiredException(((PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA)).getPlayerName());
                }
            }
        } catch (GameDoesNotExists_Exception | InvalidParameters_Exception e) {
            sendGameLogicError(response, e.getMessage());
        }

    }

    private boolean isGameOver(HttpServletRequest request) throws GameDoesNotExists_Exception {
        String gameName = ((PlayerSessionData) request.getSession().getAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA)).getRoomName();
        GameDetails gd = getWebServiceEndPoint().getGameDetails(gameName);
        
        return gd.getStatus() == GameStatus.FINISHED;
    }

    private void invalidateSession(HttpServletRequest request) {
        request.getSession().setAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA, null);
        request.getSession().invalidate();
    }
}