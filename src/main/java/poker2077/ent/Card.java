package poker2077.ent;

public class Card {
    // Specification
    private CardType type;
    private CardRank rank;

    public Card(CardType t, CardRank r) {
        this.rank = r;
        this.type = t;
    }

    public CardRank getRank() {
        return this.rank;
    }

    public CardType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type.toString() + "[" + this.rank.toString()+ "]";
    }
}


