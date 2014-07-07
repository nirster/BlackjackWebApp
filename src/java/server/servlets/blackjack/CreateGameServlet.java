package server.servlets.blackjack;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ws.blackjack.DuplicateGameName_Exception;
import ws.blackjack.InvalidParameters_Exception;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
@WebServlet(name="CreateGameServlet", urlPatterns={"/createGame"})
public final class CreateGameServlet extends BlackjackServletBase {
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (PrintWriter out = response.getWriter()) {
            try {
                request.getSession().invalidate();
                response.setContentType("application/json ;charset=UTF-8");
                int humanPlayers = extractInt(request, RequestKeys.HUMAN_PLAYERS, 0);
                int computerPlayers = extractInt(request, RequestKeys.COMPUTER_PLAYERS, 0);
                String roomName = extractString(request, RequestKeys.ROOM_NAME, "");
                Logger.getLogger("CreateGameServlet").log(Level.INFO, "Got game creation request: "
                        + "humanPlayers:{0} computerPlayers:{1} roomName:{2}", new Object[]{humanPlayers, computerPlayers, roomName});
                getWebServiceEndPoint().createGame(roomName, humanPlayers, computerPlayers);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("wasCreated", true);
                out.write(jsonObject.toString());
            } catch (DuplicateGameName_Exception | InvalidParameters_Exception ex) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("wasCreated", false);
                jsonObject.addProperty("error", ex.getMessage());
            }
        }
    }
   
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST); // Use only POST.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    public String getServletInfo() {
        return "Create Game Servlet";
    }
}