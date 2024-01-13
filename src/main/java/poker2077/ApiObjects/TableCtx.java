package poker2077.ApiObjects;

import poker2077.ent.Card;

import java.util.ArrayList;
import java.util.List;

public class TableCtx {
    public long pool = 0;
    public long deposit = 0;

    public List<CardCtx> flow;

    public TableCtx(long pool, long deposit, List<Card> flow) {
        this.pool = pool;
        this.deposit = deposit;
        this.flow = new ArrayList<>();
        for(Card c: flow) {
            this.flow.add(new CardCtx(c));
        }
    }
}
