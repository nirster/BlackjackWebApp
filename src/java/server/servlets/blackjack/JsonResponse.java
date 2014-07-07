package server.servlets.blackjack;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class JsonResponse {
    private final boolean error;
    private final String message;
    
    public JsonResponse(boolean isError, String message) {
        this.error = isError;
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
