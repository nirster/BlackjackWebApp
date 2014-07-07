package server.servlets.blackjack;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ws.blackjack.GameDetails;
import ws.blackjack.GameDoesNotExists_Exception;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

/* Returns a JSON list with all available games on server.
   No need to log in.                                      
*/
@WebServlet(name="GamesListServlet", urlPatterns={"/gamesList"})
public final class GamesListServlet extends BlackjackServletBase {
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType("application/json ;charset=UTF-8");
            Type gameDetailsListType = new TypeToken<ArrayList<GameDetails>>() {}.getType();
            List<String> games = getWebServiceEndPoint().getWaitingGames();
            games.addAll(getWebServiceEndPoint().getActiveGames());
            List<GameDetails> gamesDetails = new ArrayList<>();
            for (String gameName : games) {
                gamesDetails.add(getWebServiceEndPoint().getGameDetails(gameName));
            }
            String result = new Gson().toJson(gamesDetails, gameDetailsListType);
            response.getWriter().write(result);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (GameDoesNotExists_Exception ex) {
            sendJsonError(response, ex.getMessage());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST); // Use GET.
    }

    @Override
    public String getServletInfo() {
        return "Games List Servlet";
    }
}