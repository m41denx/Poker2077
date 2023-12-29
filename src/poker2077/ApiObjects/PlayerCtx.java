package poker2077.ApiObjects;

public class PlayerCtx {
    public String name;
    public long balance;
    public long deposit;
    public boolean folded;

    public boolean isActive;

    public PlayerCtx(String name,  long balance, long deposit, boolean folded, boolean isActive) {
        this.balance = balance;
        this.name = name;
        this.deposit = deposit;
        this.folded = folded;
        this.isActive = isActive;
    }
}
