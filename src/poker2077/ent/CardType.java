package poker2077.ent;

public enum CardType {
    Hearts("h"), // ❤️
    Diamonds("d"), // ♦️
    Clubs("c"), // ♣️
    Spades("s") // ♠️
    ;

    public final String sym;

    private CardType(String sym) {
        this.sym=sym;
    }

    @Override
    public String toString() {
        return sym;
    }
}
