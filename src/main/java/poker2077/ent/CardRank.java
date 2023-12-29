package poker2077.ent;

public enum CardRank {
    Ace("A"), King("K"), Queen("Q"), Jack("J"),
    N10("10"), N9("9"), N8("8"), N7("7"), N6("6"),
    N5("5"), N4("4"), N3("3"), N2("2"), N1("1");

    public final String sym;

    private CardRank(String sym) {
        this.sym=sym;
    }

    @Override
    public String toString() {
        return sym;
    }
}
