package poker2077;

import poker2077.ent.Card;
import poker2077.ent.Event;

import java.util.List;

public interface IPlayerManager {
    List<Card> peek();
    void giveCard(Card card);
    void reset();

    void fold();
    boolean isFolded();
    void onLoop(Event e, GameManager mgr);
    void automate(GameManager mgr);
    long getBank();
    void setBank(long bank);

    long getDeposit();
    void setDeposit(long deposit);
    String getName();
}
