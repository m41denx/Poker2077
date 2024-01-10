package poker2077.ent;


// Весов не будет, будет .ordinal()
public enum Combo {
    None("Ничего"),
    HighCard("Старшая карта"),
    Pair("Пара"),
    DoublePair("Две пары"),
    Set("Тройка"),
    Straight("Стрит"),
    Flush("Флэш"),
    FullHouse("Фулл Хаус"),
    FourOfKind("Каре"),
    StraithFlush("Стрит-флэш"),
    RoyalFlush("Флэш-рояль");

    public final String msg;

    private Combo(String msg) {
        this.msg=msg;
    }

    @Override
    public String toString() {
        return msg;
    }
}
