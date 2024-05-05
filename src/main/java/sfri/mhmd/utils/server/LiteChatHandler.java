package sfri.mhmd.utils.server;

import com.sun.net.httpserver.HttpExchange;

public interface LiteChatHandler {
    public default String getPath() {
        return "/";
    }

    public default boolean matches(HttpExchange x) {
        return true;
    }

    public void handle(HttpExchange x) throws Throwable;
}
