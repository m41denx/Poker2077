package poker2077.ApiObjects;

public class GenericCtx {
    public boolean success;
    public String error;

    public GenericCtx(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
}
