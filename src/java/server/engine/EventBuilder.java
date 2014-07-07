package server.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import server.engine.utils.Converter;
import server.engine.utils.IDGenerator;
import ws.blackjack.Action;
import ws.blackjack.Event;
import ws.blackjack.EventType;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class EventBuilder {
    private final IDGenerator idGenenrator;

    public EventBuilder() {
        idGenenrator = new IDGenerator();
    }

    public int getPlayerId() {
        return idGenenrator.getPlayerId();
    }

    public int getEventId() {
        return idGenenrator.getEventId();
    }

    public Event createGameStartEvent() {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setType(EventType.GAME_START);

        return event;
    }

    public Event createNewRoundEvent() {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setType(EventType.NEW_ROUND);

        return event;
    }

    public Event createPlaceBetEvent(String playerName, float wager) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setPlayerName(playerName);
        event.setMoney(wager);
        event.setType(EventType.USER_ACTION);
        event.setPlayerAction(Action.PLACE_BET);

        return event;
    }

    public Event createHitEvent(String playerName, Card... cards) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setPlayerName(playerName);
        event.setType(EventType.USER_ACTION);
        event.setPlayerAction(Action.HIT);
        List<Card> list = new ArrayList<>(Arrays.asList(cards));
        event.getCards().addAll(Converter.toWsCards(list));

        return event;
    }

    public Event createStandEvent(String playerName) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setPlayerName(playerName);
        event.setType(EventType.USER_ACTION);
        event.setPlayerAction(Action.STAND);

        return event;
    }

    public Event createResignEvent(String playerName) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setPlayerName(playerName);
        event.setType(EventType.PLAYER_RESIGNED);

        return event;
    }

    Event createPlayerTurnEvent(String playerName) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setPlayerName(playerName);
        event.setType(EventType.PLAYER_TURN);

        return event;
    }

    public Event createPromptForActionEvent(String playerName) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(30);
        event.setPlayerName(playerName);
        event.setType(EventType.PROMPT_PLAYER_TO_TAKE_ACTION);

        return event;
    }

    public Event createDoubleEvent(String playerName, Card card) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setPlayerName(playerName);
        event.setType(EventType.USER_ACTION);
        event.setPlayerAction(Action.DOUBLE);
        event.getCards().add(Converter.toWsCard(card));

        return event;
    }

    public Event createSplitEvent(String playerName, Hand firstHand, Hand secondHand) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setPlayerName(playerName);
        event.setType(EventType.USER_ACTION);
        event.setPlayerAction(Action.SPLIT);
        event.getCards().add(Converter.toWsCard(firstHand.getCards().get(0)));
        event.getCards().add(Converter.toWsCard(secondHand.getCards().get(0)));

        return event;
    }

    Event createGameOverEvent() {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setType(EventType.GAME_OVER);

        return event;
    }

    public Event createCardsDealtEvent(String playerName, Card... cards) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setPlayerName(playerName);
        event.getCards().addAll(Converter.toWsCards(Arrays.asList(cards)));
        event.setType(EventType.CARDS_DEALT);

        return event;
    }

    public Event createGameWinnerEvent(String name, float wager) {
        Event event = new Event();

        event.setId(idGenenrator.getEventId());
        event.setTimeout(0);
        event.setType(EventType.GAME_WINNER);
        event.setMoney(wager);
        event.setPlayerName(name);

        return event;
    }
}