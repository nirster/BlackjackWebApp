package server.engine.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.engine.Card;
import server.engine.enums.HandAction;
import server.engine.enums.PlayerStatus;
import server.engine.enums.PlayerType;
import ws.blackjack.Action;
import ws.blackjack.InvalidParameters_Exception;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

/* This utlity class is responsible to convert our local types/classed to those generated from WSDL. */
public final class Converter {
    
    public static HandAction toHandAction(Action action) {
        HandAction handAction = null;
        
        switch (action) {
            case PLACE_BET:
                handAction = HandAction.PLACE_BET;
                break;
            case HIT:
                handAction = HandAction.HIT;
                break;
            case DOUBLE:
                handAction = HandAction.DOUBLE;
                break;
            case STAND:
                handAction = HandAction.STAND;
                break;
            case SPLIT:
                handAction = HandAction.SPLIT;
                break;
        }
        
        return handAction;
                       
    }
    
    public static ws.blackjack.Card toWsCard(Card theCard) {
        ws.blackjack.Card resultCard = new ws.blackjack.Card();

        try {
            resultCard.setRank(Converter.toWsRank(theCard.getRank()));
            resultCard.setSuit(Converter.toWsSuit(theCard.getSuit()));
        } catch (InvalidParameters_Exception ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resultCard;
    }
    
    public static List<ws.blackjack.Card> toWsCards(List<Card> cardsList) {
        List<ws.blackjack.Card> retList = new ArrayList<>();

        for (Card card : cardsList) {
            ws.blackjack.Card wsCard = toWsCard(card);
            retList.add(wsCard);
        }

        return retList;
    }
    
    public static ws.blackjack.Suit toWsSuit(Card.Suit theSuit) throws InvalidParameters_Exception {
        switch (theSuit) {
            case CLUBS:
                return ws.blackjack.Suit.CLUBS;
            case DIAMONDS:
                return ws.blackjack.Suit.DIAMONDS;
            case HEARTS:
                return ws.blackjack.Suit.HEARTS;
            case SPADES:
                return ws.blackjack.Suit.SPADES;
            default:
                throw new InvalidParameters_Exception("no such suit", null);
        }
    }
    
    public static ws.blackjack.Rank toWsRank(Card.Rank theRank) throws InvalidParameters_Exception {
        switch (theRank) {
            case ACE:
                return ws.blackjack.Rank.ACE;
            case TWO:
                return ws.blackjack.Rank.TWO;
            case THREE:
                return ws.blackjack.Rank.THREE;
            case FOUR:
                return ws.blackjack.Rank.FOUR;
            case FIVE:
                return ws.blackjack.Rank.FIVE;
            case SIX:
                return ws.blackjack.Rank.SIX;
            case SEVEN:
                return ws.blackjack.Rank.SEVEN;
            case EIGHT:
                return ws.blackjack.Rank.EIGHT;
            case NINE:
                return ws.blackjack.Rank.NINE;
            case TEN:
                return ws.blackjack.Rank.TEN;
            case JACK:
                return ws.blackjack.Rank.JACK;
            case QUEEN:
                return ws.blackjack.Rank.QUEEN;
            case KING:
                return ws.blackjack.Rank.KING;
            default:
                throw new InvalidParameters_Exception("no such rank", null);
        }
    }
    
    public static ws.blackjack.PlayerType toWsPlayerType(PlayerType theType) {
        ws.blackjack.PlayerType result = ws.blackjack.PlayerType.COMPUTER;
        
        switch (theType) {
            case COMPUTER:
                result = ws.blackjack.PlayerType.COMPUTER;
                break;
            case HUMAN:
                result = ws.blackjack.PlayerType.HUMAN;
                break;
        }
        
        return result;
    }
    
    public static ws.blackjack.PlayerStatus toWsPlayerStatus(PlayerStatus theStatus) {
        ws.blackjack.PlayerStatus result = ws.blackjack.PlayerStatus.JOINED;
        
        switch (theStatus) {
            case ACTIVE:
                result = ws.blackjack.PlayerStatus.ACTIVE;
                break;
            case JOINED:
                result = ws.blackjack.PlayerStatus.JOINED;
                break;
            case RETIRED:
                result = ws.blackjack.PlayerStatus.RETIRED;
                break;
        }
        
        return result;
    }
}