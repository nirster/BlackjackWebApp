package server.engine;

import server.engine.Card.Rank;
import server.engine.Card.Suit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cards deck implementation
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class Deck {
    private final List<Card> cardsList;
    private final Random randomizer;
    
    public Deck() {
        this.cardsList = new ArrayList<>(Settings.CARDS_IN_DECK);
        randomizer = new Random();
        init();
        shuffle();
    }
    
    public Card drawCard() {
        int randomIndex = randomizer.nextInt(Settings.CARDS_IN_DECK);
        return new Card(cardsList.get(randomIndex));
    }
    
    public void shuffle() {
        randomizer.setSeed(System.currentTimeMillis());
    }

    private void init() {
        Rank[] ranks = Rank.values();
        Suit[] suits = Suit.values();
        
        for (int i = 0; i < Settings.SUITS_IN_DECK; ++i) {
            for (int j = 0; j < Settings.RANKS_IN_DECK; ++j) {
                this.cardsList.add(new Card(suits[i], ranks[j]));
            } 
        }
    }
}