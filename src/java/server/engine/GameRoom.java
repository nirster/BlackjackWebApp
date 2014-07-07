package server.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import static server.engine.Settings.FIRST_HAND;
import server.engine.enums.GameMode;
import server.engine.enums.PlayerStatus;
import server.engine.enums.PlayerType;
import server.engine.exceptions.GameAlreadyStartedException;
import server.engine.exceptions.GameException;
import server.engine.exceptions.GameOverException;
import server.engine.exceptions.GameRoomFullException;
import server.engine.exceptions.InvalidActionException;
import server.engine.exceptions.InvalidEventIdException;
import server.engine.exceptions.InvalidXMLPlayerName;
import server.engine.exceptions.PlayerExistsException;
import server.engine.exceptions.PlayerNotFoundException;
import ws.blackjack.Action;
import ws.blackjack.Event;
import ws.blackjack.EventType;
import ws.blackjack.GameStatus;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class GameRoom {
    private final EventManager eventManager;
    private final GameContext gameContext;
    private final GameConfig gameConfig;
    private final List<TimedEvent> timedEvents;
    private List<Player> players;
    private Player dealer;
    private Player currentPlayer;
    private GameOverListener gameOverListener;

    public GameRoom(String name, int humanPlayers, int computerPlayers) {
        this.eventManager = new EventManager();
        this.gameContext = new GameContext();
        this.gameConfig = new GameConfig(name, humanPlayers, computerPlayers);
        this.players = new ArrayList<>();
        timedEvents = new ArrayList<>();
        joinComputerPlayersAndDealer(computerPlayers);
    }
    
    public GameRoom(GameConfig gameConfig) {
        this.eventManager = new EventManager();
        this.gameContext = new GameContext();
        this.players = new ArrayList<>();
        this.timedEvents = new ArrayList<>();
        this.gameConfig = gameConfig;
        joinComputerPlayersAndDealer(gameConfig.getComputerPlayers());
    }
    
    public void setGameOverListener(GameOverListener l) {
        gameOverListener = l;
    }

    public void doPlayerAction(int playerId, int eventId, Action action, float money, int bet) throws GameException {
        throwIfGameIsOver();
        throwIfInvalidAction(playerId, action, money, bet);
        throwIfInvalidId(playerId);
        throwIfInvalidEventId(eventId);
        
        String playerName = getPlayer(playerId).getName();
        stopTimer(playerName);
        HumanPlayer humanPlayer = (HumanPlayer) getPlayer(playerId);
        Event event = humanPlayer.doAction(action, money, bet, gameContext);
        addEvent(event);
        afterPlayerAction();
    }
    
    private void afterPlayerAction() {
        if (isCurrentPlayerCanDoMoreMove()) {
            askHumanForAction();
        }
        else if (isMorePlayersCanPlay()) {
            moveToNextPlayerThatCanPlay();
            askHumanForAction();
        } else { // if we are here it means that either: 
                 // 1) everyone placed their bets, and waiting to MID_ROUND mode                                     
                 // 2) everyone finished their actual move, and waiting for game resolution
            GameMode currentGameMode = getGameMode();
            switch (currentGameMode) {
                case PLACING_BETS:
                    dealCards();
                    setGameMode(GameMode.MID_ROUND);
                    playComputerMoves();
                    break;
                case MID_ROUND:
                    playDealerFinalMove();
                    solveAllBets();
                    clearTimers();
                    startNewRoundWithDelay();
                    break;
            }
        }
    }
    
    private void startNewRoundWithDelay() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                initNewRound();
            }
        };
        
        timer.schedule(timerTask, 9 * 1000);
    }
    
    private void initNewRound() {
        clearResignedPlayers();
        
        if (getActiveHumanPlayers().isEmpty()) {
            fireGameOverCallback();
            return;
        }
        // else
        currentPlayer = getPlayers().get(0);
        addEvent(getEventBuilder().createNewRoundEvent());
        setGameStatus(GameStatus.ACTIVE);
        setGameMode(GameMode.PLACING_BETS);
        for (Player player : getPlayers()) {
            player.initNewRound();
        }
        
        dealer.initNewRound();
        playComputerBets();
    }
    
    private boolean isMorePlayersCanPlay() {
        int currentPlayerIndex = getPlayers().indexOf(currentPlayer);
        int activePlayersCount = 0;
        int nextPlayerIndex = currentPlayerIndex + 1;
        
        if (nextPlayerIndex < getPlayers().size()) {
            for (int i = nextPlayerIndex; i < getPlayers().size(); ++i) {
                Player p = getPlayers().get(i);
                switch (getGameMode()) {
                    case PLACING_BETS:
                        if (p.getStatus() != PlayerStatus.RETIRED && !p.isPlacedBet()) {
                            activePlayersCount++;
                        }
                        break;
                    case MID_ROUND:
                        if (p.getStatus() != PlayerStatus.RETIRED && p.canDoMoreMoves()) {
                            activePlayersCount++;
                        }
                        break;
                }
            }
        }
        
        return activePlayersCount >= 1;
    }
    // should be called only after isMorePlayersCanPlay()
    private void moveToNextPlayerThatCanPlay() {
        int index = getPlayers().indexOf(currentPlayer);
        ++index; // this is why it must be called only after isMorePlayersCanPlay()
        
        for (int i = index; i < getPlayers().size(); ++i) {
            Player p = getPlayers().get(i);
            switch (getGameMode()) {
                case PLACING_BETS:
                    if (p.getStatus() != PlayerStatus.RETIRED && !p.isPlacedBet()) {
                        currentPlayer = p;
                        return;
                    }
                    break;
                case MID_ROUND:
                    if (p.getStatus() != PlayerStatus.RETIRED && p.canDoMoreMoves()) {
                        currentPlayer = p;
                        return;
                    }
                    break;
            }
        }
    }
    
    private void askHumanForAction() {
        Event event = currentPlayer.askForAction(gameContext);
        addEvent(event);
        startTimer(event, currentPlayer.getId());
    }

    private void startTimer(final Event event, int playerId) {
        timedEvents.add(new TimedEvent(this, event, playerId));
    }

    private void stopTimer(String playerName) {
        for (TimedEvent timedEvent : timedEvents) {
            if (timedEvent.getName().equals(playerName)) {
                timedEvent.cancel();
            }
        }
    }
    
    private void clearTimers() {
        for (TimedEvent timedEvent : timedEvents) {
           timedEvent.cancel();
        }
        
        timedEvents.clear();
    }

    private void playComputerBets() {
        for (Player player : getPlayers()) {
            currentPlayer = player;
            addEvent(getEventBuilder().createPlayerTurnEvent(player.getName()));
            final Event event = player.askForAction(gameContext);
            addEvent(event);
            
            if (event.getType() == EventType.PROMPT_PLAYER_TO_TAKE_ACTION) {
                startTimer(event, currentPlayer.getId());
                return;
            }
        }
    }

    private void playComputerMoves() {
        for (Player player : getPlayers()) {
            currentPlayer = player;
            Event turnEvent = getEventBuilder().createPlayerTurnEvent(player.getName());
            addEvent(turnEvent);
            
            if (player.getType() == PlayerType.COMPUTER) {
                while (player.canDoMoreMoves()) {
                    Event moveEvent = player.askForAction(gameContext);
                    addEvent(moveEvent);
                }
            }
            
            else if (player.getType() == PlayerType.HUMAN) {
                Event promptEvent = player.askForAction(gameContext);
                addEvent(promptEvent);
                startTimer(promptEvent, currentPlayer.getId());
                return;
            }
        }
    }

    public int joinPlayer(String name, float money) throws GameException {
        throwIfInvalidXMLName(name);
        throwIfNameExists(name);
        throwIfGameIsFull();
        throwIfGameIsActive();
        throwIfGameIsOver();
        
        int playerId = getEventBuilder().getPlayerId();
        Player player = new HumanPlayer(name, money, playerId);
        players.add(player);
        gameConfig.incrementJoinedPlayers();
        if (getJoinedHumanPlayers() == getHumanPlayers()) {
            setGameStatus(GameStatus.ACTIVE);
            addEvent(getEventBuilder().createGameStartEvent());
            playComputerBets();
        }
        
        return playerId;
    }

    private void playDealerFinalMove() {
        while (dealer.canDoMoreMoves()) {
            final Event event = dealer.askForAction(gameContext);
            addEvent(event);
        }
    }

    private void solveAllBets() {
        Hand dealerHand = dealer.getHands().get(FIRST_HAND);
        int dealerHandValue = dealerHand.getValue();
        BetSolver betSolver = new BetSolver(gameContext, dealerHandValue);
        
        for (Player player : getPlayers()) {
            for (Hand hand : player.getHands()) {
                betSolver.solve(player, hand);
            }
        }
        
        for (Event event : betSolver.getResults()) {
            addEvent(event);
        }
    }

    private synchronized void addEvent(Event event) {
        eventManager.addEvent(event);
    }

    private void dealCards() {
        for (Player player : getPlayers()) {
            if (player.getStatus() != PlayerStatus.RETIRED) {
                Event event = player.dealInitialCards(gameContext);
                addEvent(event);
            }
        }
        
        Event event = dealer.askForAction(gameContext);
        addEvent(event);
    }

    public void doResign(int playerId) {
        Player resignedPlayer = getPlayer(playerId);
        resignedPlayer.setStatus(PlayerStatus.RETIRED);
        addEvent(getEventBuilder().createResignEvent(resignedPlayer.getName()));
        stopTimer(resignedPlayer.getName());
        
        int resignedPlayerIndex = getPlayers().indexOf(resignedPlayer);
        int currentPlayerIndex = getPlayers().indexOf(currentPlayer);
        
        if (currentPlayerIndex == resignedPlayerIndex) {
            afterPlayerAction();
        }
        
        if (getActiveHumanPlayers().isEmpty()) {
            gameContext.setGameStatus(GameStatus.FINISHED);
            fireGameOverCallback();
        }
    }

    private void joinComputerPlayersAndDealer(int count) {
        for (int i = 0; i < count; ++i) {
            Player pcPlayer = new ComputerPlayer("PC" + Integer.toString(i + 1), gameConfig.getMoney(), getEventBuilder().getPlayerId());
            players.add(pcPlayer);
        }
        
        dealer = new Dealer(getEventBuilder().getPlayerId());
    }

    public boolean containsPlayer(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                return true;
            }
        }
        
        return false;
    }

    private void throwIfNameExists(String playerName) {
        for (Player player : players) {
            if (player.getName().equals(playerName)) {
                throw new PlayerExistsException(playerName, gameConfig.getName());
            }
        }
    }

    public int getHumanPlayers() {
        return gameConfig.getHumanPlayers();
    }

    public int getJoinedHumanPlayers() {
        return gameConfig.getJoinedHumanPlayers();
    }

    public int getComputerPlayers() {
        return gameConfig.getComputerPlayers();
    }

    public String getName() {
        return gameConfig.getName();
    }

    public boolean isLoadedFromXML() {
        return gameConfig.isLoadedFromXML();
    }

    public GameStatus getStatus() {
        return gameContext.getGameStatus();
    }

    public float getMoney() {
        return gameConfig.getMoney();
    }

    public synchronized List<Event> getEvents(int startId) {
        final List<Event> eventsList = new ArrayList<>();
        for (Event event : eventManager.getEvents()) {
            if (event.getId() > startId) {
                eventsList.add(event);
            }
        }
        
        return eventsList;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getDealer() {
        return this.dealer;
    }

    private EventBuilder getEventBuilder() {
        return gameContext.getEventBuilder();
    }

    private GameMode getGameMode() {
        return gameContext.getGameMode();
    }

    private void setGameMode(GameMode gameMode) {
        gameContext.setGameMode(gameMode);
    }

    private List<Player> getActiveHumanPlayers() {
        List<Player> activeHumanPlayers = new ArrayList<>();
        
        for (Player player : players) {
            if (player.getType() == PlayerType.HUMAN && player.getStatus() != PlayerStatus.RETIRED) {
                activeHumanPlayers.add(player);
            }
        }
        
        return activeHumanPlayers;
    }

    private Player getPlayer(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        
        throw new PlayerNotFoundException(playerId, getName());
    }

    private void throwIfGameIsFull() {
        if (getJoinedHumanPlayers() >= getHumanPlayers()) {
            throw new GameRoomFullException(getName(), getJoinedHumanPlayers(), getHumanPlayers());
        }
    }

    private void throwIfGameIsActive() {
        if (gameContext.getGameStatus() != GameStatus.WAITING) {
            throw new GameAlreadyStartedException(getName());
        }
    }

    private void throwIfInvalidXMLName(String name) throws InvalidXMLPlayerName {
        if (!gameConfig.isValidXMLPlayerName(name)) {
            throw new InvalidXMLPlayerName(name);
        }
    }

    private void throwIfGameIsOver() throws GameException {
        if (gameContext.getGameStatus() == GameStatus.FINISHED) {
            throw new GameOverException(gameConfig.getName());
        }
    }

    private void throwIfInvalidId(int playerId) {
        if (getPlayer(playerId).getType() != PlayerType.HUMAN) {
            throw new PlayerNotFoundException(playerId, getName());
        }
    }

    private void throwIfInvalidEventId(int eventId) throws GameException {
        for (TimedEvent timedEvent : timedEvents) {
            if (timedEvent.getId() == eventId) {
                return;
            }
        }
        
        throw new InvalidEventIdException(eventId);
    }
    
    private void throwIfInvalidAction(int playerId, Action action, float money, int bet) throws GameException {
        Player player = getPlayer(playerId);
        if (getGameMode() == GameMode.PLACING_BETS && action != Action.PLACE_BET) {
            throw new InvalidActionException(action, player.getName());
        }
        
        if (getGameMode() == GameMode.PLACING_BETS && action == Action.PLACE_BET) {
            if (player.getMoney() < money) {
                throw new InvalidActionException(action, player.getName());
            }
        }

        if (getGameMode() == GameMode.MID_ROUND && !player.canDoAction(action, money, bet)) {
            throw new InvalidActionException(action, player.getName());
        }

        if (player.getStatus() == PlayerStatus.RETIRED) {
            throw new InvalidActionException(action, player.getName());
        }
    }

    private void setGameStatus(GameStatus gameStatus) {
        gameContext.setGameStatus(gameStatus);
    }

    private void fireGameOverCallback() {
        gameContext.setGameStatus(GameStatus.FINISHED);
        addEvent(getEventBuilder().createGameOverEvent());
        clearTimers();
        
        if (gameOverListener != null) {
            gameOverListener.gameOver(getName());
        }
    }
    
    private void clearResignedPlayers() {
        List<Player> newList = new ArrayList<>();
        
        for (Player player : getPlayers()) {
            if (player.getStatus() != PlayerStatus.RETIRED) {
                newList.add(player);
            }
        }
        
        players = null;
        players = newList;
    }

    private boolean isCurrentPlayerCanDoMoreMove() {
        return getGameMode() == GameMode.MID_ROUND && currentPlayer.canDoMoreMoves();
    }
}