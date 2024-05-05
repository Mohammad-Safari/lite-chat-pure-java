package sfri.mhmd.utils.server;

import java.net.InetSocketAddress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerConfiguration {
    private final String context;

    private final InetSocketAddress addresss;
}
