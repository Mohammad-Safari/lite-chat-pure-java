package sfri.mhmd.utils.server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import sfri.mhmd.utils.cdi.anno.Inject;

public class LiteChatHttpServer {
    private final HttpServer server;
    private final List<LiteChatHandler> handlers;

    @Inject
    public LiteChatHttpServer(ServerConfiguration serverConfiguration, List<LiteChatHandler> handlers)
            throws IOException {
        this.handlers = handlers;
        this.server = HttpServer.create(serverConfiguration.getAddresss(), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext(serverConfiguration.getContext(), (exchange) -> handleContextRequests(exchange));
    }

    public void start() throws IOException {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private void handleContextRequests(HttpExchange exchange) {
        var path = exchange.getRequestURI().getPath();
        var handlerOptional = handlers.stream()
                .filter((h) -> path.matches(h.getPath()))
                .filter((h) -> h.matches(exchange))
                .findFirst();
        handlerOptional.ifPresent((handler) -> {
            try {
                handler.handle(exchange);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        exchange.close();
    }

}
