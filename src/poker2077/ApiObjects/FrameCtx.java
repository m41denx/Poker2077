package poker2077.ApiObjects;

import poker2077.ent.Card;
import poker2077.ent.Event;

import java.util.ArrayList;
import java.util.List;

public class FrameCtx {
    public List<PlayerCtx> players;
    public PlayerCtx currentPlayer;

    public boolean isAdmin;
    public List<CardCtx> hand;

    public TableCtx table;
    public List<Event> evtLoop;

    public FrameCtx(TableCtx table, List<Event> evtLoop) {
        this.table = table;
        players = new ArrayList<>();
        this.evtLoop = evtLoop;
    }

    public void addPlayer(PlayerCtx p) {
        players.add(p);
    }
    public void setCurrentPlayer(PlayerCtx p, List<Card> hand) {
        currentPlayer = p;
        this.hand = new ArrayList<>();
        for(Card c: hand) {
            this.hand.add(new CardCtx(c));
        }
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
