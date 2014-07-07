package server.servlets.blackjack;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import ws.blackjack.DuplicateGameName_Exception;
import ws.blackjack.InvalidParameters_Exception;
import ws.blackjack.InvalidXML_Exception;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

@WebServlet(name = "UploadServlet", urlPatterns = {"/upload"})
@MultipartConfig
public final class UploadServlet extends BlackjackServletBase {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json ;charset=UTF-8");
        Part filePart = request.getPart("file");
        InputStream fileContent = filePart.getInputStream();
        String fileString = getStringFromInputStream(fileContent);
        
        try (PrintWriter out = response.getWriter()) {
            try {
                request.getSession().invalidate();
                response.setContentType("application/json ;charset=UTF-8");
                getWebServiceEndPoint().createGameFromXML(fileString);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("wasCreated", true);
                out.write(jsonObject.toString());
            } catch (DuplicateGameName_Exception | InvalidParameters_Exception ex) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("wasCreated", false);
                jsonObject.addProperty("error", ex.getMessage());
                
            } catch (InvalidXML_Exception ex) {
                Logger.getLogger(UploadServlet.class.getName()).log(Level.SEVERE, null, ex);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("wasCreated", false);
                jsonObject.addProperty("error", ex.getMessage());
            }
        }
     
    }
    
    private String getStringFromInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            
            return sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(UploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
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
        return "File Upload Servlet";
    }
}