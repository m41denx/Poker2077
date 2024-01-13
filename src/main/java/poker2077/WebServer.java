package poker2077;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;

import java.util.UUID;

public class WebServer {

    private GameManager mgr = null;
    private ObjectMapper mapper = new ObjectMapper();

    WebServer(GameManager mgr) {
        this.mgr = mgr;
    }

    void Start(int port, String frontendPath) {
        var app = Javalin.create(cfg -> cfg.addStaticFiles(frontendPath, Location.EXTERNAL))
                .get("/status", ctx->{
                    ctx.result(mapper.writeValueAsString(mgr.getStatus()));
                })
                .get("/new", ctx->{
                    String cook = UUID.randomUUID().toString();
                    ctx.cookie("session", cook);
                    ctx.result(mapper.writeValueAsString(mgr.newGame(cook)));
                })
                .get("/end", ctx-> {
                    ctx.result(mapper.writeValueAsString(mgr.terminate(ctx.cookie("session"))));
                })
                .get("/join", ctx->{
                    String cook = ctx.cookie("session");
                    if (cook==null || cook.isEmpty()) {
                        cook = UUID.randomUUID().toString();
                        ctx.cookie("session", cook);
                    }
                    ctx.result(mapper.writeValueAsString(mgr.joinGame(cook)));
                })
                .get("/joinbot", ctx->{
                    ctx.result(mapper.writeValueAsString(mgr.joinBot(ctx.cookie("session"))));
                })
                .get("/getframe", ctx->{
                    ctx.result(mapper.writeValueAsString(mgr.getFrame(ctx.cookie("session"))));
                })
                .get("/fold", ctx->{
                    ctx.result(mapper.writeValueAsString(mgr.fold(ctx.cookie("session"))));
                })
                .get("/call", ctx->{
                    ctx.result(mapper.writeValueAsString(mgr.call(ctx.cookie("session"))));
                })
                .get("/raise", ctx->{
                    var ssum = ctx.queryParam("amount");
                    if (ssum==null) return;
                    long sum = Integer.parseInt(ssum);
                    ctx.result(mapper.writeValueAsString(mgr.raise(ctx.cookie("session"), sum)));
                })
                .start(port);
    }
}
