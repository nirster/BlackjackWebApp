package server.servlets.blackjack;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ws.blackjack.GameDoesNotExists_Exception;
import ws.blackjack.InvalidParameters_Exception;
import ws.blackjack.InvalidXML_Exception;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public final class LoginServlet extends BlackjackServletBase {
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger logger = Logger.getLogger(this.getClass().getName());
        
        try {
            response.setContentType("application/json ;charset=UTF-8");
            if (isRequestContainsKeys(request, RequestKeys.GAME_ACTION, RequestKeys.ROOM_NAME, RequestKeys.PLAYER_NAME)) {
                if (extractString(request, RequestKeys.GAME_ACTION, "").equals(RequestValues.JOIN)) {
                    String playerName = extractString(request, RequestKeys.PLAYER_NAME, "");
                    String roomName = extractString(request, RequestKeys.ROOM_NAME, "");
                    int playerMoney = extractInt(request, RequestKeys.PLAYER_MONEY, 1000);
                    int playerId = getWebServiceEndPoint().joinGame(roomName, playerName, playerMoney);
                    PlayerSessionData playerSessionData = new PlayerSessionData(playerId, playerName, roomName);
                    request.getSession().setMaxInactiveInterval(25);
                    request.getSession().setAttribute(SessionAttrsKeys.PLAYER_SESSION_DATA, playerSessionData);
                    logger.log(Level.INFO, "creating new session for player:{0} room:{1}", new String[]{playerName, roomName});
                    logger.log(Level.INFO, "new session id:{0}", request.getSession().getId());
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("success", Boolean.TRUE);
                    jsonObject.addProperty("gamePage", "game.html");
                    response.getWriter().write(jsonObject.toString());
                    response.getWriter().close();
                }
            }
        } catch (GameDoesNotExists_Exception | InvalidParameters_Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("error", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            response.getWriter().close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST); // Use POST.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Login Servlet";
    }
}