package poker2077.ApiObjects;

public class StatusCtx {
    public boolean isActive;
    public int players;

    public StatusCtx(boolean isActive, int players) {
        this.isActive = isActive;
        this.players = players;
    }
}
