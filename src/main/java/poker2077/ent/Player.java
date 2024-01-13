package poker2077.ent;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public List<Card> hand = new ArrayList<>();
    public String name;
    public long bank = 0;
    public long deposit = 0;
    public String uuid; // Игрок должен знать свой uuid чтобы быть ботом и вызывать методы GameManager

    public boolean folded = false;
}
