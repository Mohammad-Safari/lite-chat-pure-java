package sfri.mhmd.utils.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceHandler implements LiteChatHandler {

    private final String path;

    @Override
    public boolean matches(HttpExchange x) {
        return x.getRequestMethod().equals("GET");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Path resourcePath = Path.of("resources", exchange.getRequestURI().toString());
        if (Files.exists(resourcePath)) {
            var data = Files.readAllBytes(resourcePath);
            exchange.sendResponseHeaders(200, data.length);
            exchange.getResponseBody().write(data);
        } else {
            exchange.sendResponseHeaders(404, 0);
        }
    }

}
