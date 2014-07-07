package server.engine;

import java.util.Objects;

/**
 * Card representation
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Card(Card other) {
        this(other.suit, other.rank);
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.rank);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Card other = (Card) obj;
        
        return this.rank.numericValue() == other.rank.numericValue();
    }

    public Rank getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return this.rank.toString() + this.suit.toString();
    }

    public enum Rank {
        ACE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING;

        public int numericValue() {
            int ordinalValue = this.ordinal();
            int numericValue = 0;

            if (ordinalValue == 0) {
                numericValue = 11;
            }
            if (ordinalValue >= 1 && ordinalValue <= 9) {
                numericValue = ordinalValue + 1;
            }
            if (ordinalValue <= 12 && ordinalValue >= 10) {
                numericValue = 10;
            }

            return numericValue;
        }
    }

    public enum Suit {
        DIAMONDS,
        SPADES,
        CLUBS,
        HEARTS
    }
}