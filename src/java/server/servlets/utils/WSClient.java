package server.servlets.utils;

import java.net.MalformedURLException;
import java.net.URL;
import ws.blackjack.BlackJackWebService;
import ws.blackjack.BlackJackWebService_Service;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

public final class WSClient {
    public static final String OPEN_SHIFT_URL = "http://tomcat-nirzarko.rhcloud.com/api/BlackJackWebService?wsdl";
    public static final String LOCALHOST_URL = "http://localhost/api/BlackJackWebService?wsdl";
    private BlackJackWebService_Service service;
    private BlackJackWebService blackJackPort;
    
    public WSClient() {
        this(LOCALHOST_URL);
    }
    
    public WSClient(String urlString) {
        try {
            URL serviceUrl = new URL(urlString);
            service = new BlackJackWebService_Service(serviceUrl);
            blackJackPort = service.getBlackJackWebServicePort();
        } catch (MalformedURLException e) {
            System.err.println("error opening webService connection: " + e.getMessage());
        }
    }
    
    public BlackJackWebService getBlackJackPort() {
        return blackJackPort;
    }
}