package server.engine;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import ws.blackjack.InvalidXML_Exception;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

public final class GameConfig {
    private final int humanPlayers;
    private final int computerPlayers;
    private final boolean loadedFromXML;
    private final List<String> humanPlayersNames;
    private final AtomicInteger joinedHumanPlayers = new AtomicInteger(0);
    private final String name;
    private final float money;
    
    public GameConfig(String name, int humanPlayers, int computerPlayers) {
        this.name = name;
        this.humanPlayers = humanPlayers;
        this.computerPlayers = computerPlayers;
        this.loadedFromXML = false;
        humanPlayersNames = null;
        this.money = 1000f;
    }
    
    public GameConfig(String xmlData, XmlLoader xmlLoader) throws InvalidXML_Exception {
        xmlLoader.parseXmlData(xmlData);
        this.computerPlayers = xmlLoader.getComputerPlayerCount();
        this.humanPlayers = xmlLoader.getHumanPlayerCount();
        this.loadedFromXML = true;
        this.humanPlayersNames = xmlLoader.getHumanPlayersNames();
        Logger.getLogger(getClass().getName()).log(Level.INFO, humanPlayersNames.toString());
        this.money = 1000f;
        this.name = xmlLoader.getGameName();
    }
    
    public String getName() {
        return name;
    }

    public float getMoney() {
        return money;
    }
    
    public boolean isValidXMLPlayerName(String name) {
        if (!loadedFromXML) {
            return true;
        }
        
        else {
            return isContainsPlayerName(name);
        }
    }
    
    private boolean isContainsPlayerName(String name) {
        for (String str : humanPlayersNames) {
            if (str.equalsIgnoreCase(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isLoadedFromXML() {
        return loadedFromXML;
    }
    
    public int getHumanPlayers() {
        return humanPlayers;
    }
    
    public int getComputerPlayers() {
        return computerPlayers;
    }
    
    public int getJoinedHumanPlayers() {
        return joinedHumanPlayers.get();
    }
    
    public void incrementJoinedPlayers() {
        joinedHumanPlayers.incrementAndGet();
    }
    
    public void decrementJoinedPlayers() {
        joinedHumanPlayers.decrementAndGet();
    }
}
