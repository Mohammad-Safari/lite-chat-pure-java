package sfri.mhmd.utils.server;

import com.sun.net.httpserver.HttpExchange;

public interface LiteChatController {
    public default String getPath() {
        return "/";
    }

    public default boolean matches(HttpExchange x) {
        return true;
    }

    /**
     * what lite chat server calls
     * 
     * @param x
     * @throws Throwable
     */
    public void handle(HttpExchange x) throws Throwable;
}
