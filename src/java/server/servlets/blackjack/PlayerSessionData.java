package server.servlets.blackjack;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

public final class PlayerSessionData {
        private final int playerId;
        private final String roomName;
        private final String playerName;
        private int lastEventId;
        
        public PlayerSessionData(int playerId, String playerName, String roomName) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.lastEventId = -1;
            this.roomName = roomName;
        }
        
        public void setLastEventId(int value) {
            lastEventId = value;
        }

        public int getLastEventId() {
            return lastEventId;
        }

        public int getPlayerId() {
            return playerId;
        }

        public String getRoomName() {
            return roomName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }
