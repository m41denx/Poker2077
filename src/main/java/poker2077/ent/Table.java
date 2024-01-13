package poker2077.ent;

import java.util.*;

// Все что лежит на столе: колода, флоп, общий банк и значение депозита
public class Table {

    private List<Card> deck;
    private List<Card> flow;
    private long bankPool = 0;
    private long deposit = 0;

    public Table() {
        this.deck = new ArrayList<>();
        this.flow = new ArrayList<>();
        for(var t: CardType.values()) {
            for(var r: CardRank.values()) {
                deck.add(new Card(t,r));
            }
        }

        this.shuffleDeck();
    }

    public void shuffleDeck() {
        Collections.shuffle(this.deck);
    }

    public Card popDeck() {
        if (this.deck.isEmpty()) {
            return null;
        }
        Card c = this.deck.get(0);
        this.deck.remove(0);
        return c;
    }

    public void addFlow() {
        this.flow.add(popDeck());

    }

    public List<Card> getFlow() {
        return this.flow;
    }

    public long getBankPool() {
        return bankPool;
    }

    public void setBankPool(long bankPool) {
        this.bankPool = bankPool;
    }

    public long getDeposit() {
        return deposit;
    }

    public void setDeposit(long deposit) {
        this.deposit = deposit;
    }
}

