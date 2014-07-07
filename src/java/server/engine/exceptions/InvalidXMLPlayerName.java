package server.engine.exceptions;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class InvalidXMLPlayerName extends GameException {

    public InvalidXMLPlayerName(String name) {
        super(String.format("%s is not in XML players list.", name));
    }

}
