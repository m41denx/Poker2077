package poker2077;

import poker2077.ent.Card;
import poker2077.ent.Event;
import poker2077.ent.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager implements IPlayerManager {
    protected Player state;
    public PlayerManager(String name, String uuid, long bank) {
        this.state = new Player();
        this.state.name = name;
        this.state.hand = new ArrayList<>();
        this.state.bank = bank;
        this.state.uuid = uuid;
    }
    public List<Card> peek() {
        return this.state.hand;
    }

    public void giveCard(Card card) {
        this.state.hand.add(card);
    }

    public void reset() {
        this.state.hand = new ArrayList<>();
        this.state.folded = false;
        this.state.deposit = 0;
    }

    public void fold() {
        this.state.folded = true;
    }

    public boolean isFolded() {
        return this.state.folded || this.state.bank == 0;
    }


    public void onLoop(Event e, GameManager mgr) {}
    public void automate(GameManager mgr) {}

    public long getBank() {
        return this.state.bank;
    }

    public void setBank(long bank) {
        this.state.bank = bank;
    }

    public long getDeposit() {
        return this.state.deposit;
    }

    public void setDeposit(long deposit) {
        this.state.deposit = deposit;
    }

    public String getName() {
        return this.state.name;
    }
}
