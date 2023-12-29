package poker2077.ApiObjects;

import poker2077.ent.Card;

public class CardCtx {
    public String rank;
    public String type;

    public CardCtx(Card card) {
        rank = card.getRank().toString();
        type = card.getType().toString();
    }
}
