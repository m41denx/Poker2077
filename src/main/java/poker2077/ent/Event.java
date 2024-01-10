package poker2077.ent;

import java.util.HashMap;
import java.util.Map;

// События нужны для уведомлений на фронте и для ботов (попробуйте сделать ставку в 200% от депозита, они же инфаркт поймают)
public class Event {

    public EventType type;
    public String text;
    public long amount;

    public Event(EventType type, String text, long amount) {
        this.type = type;
        this.text = text;
        this.amount = amount;
    }
}
