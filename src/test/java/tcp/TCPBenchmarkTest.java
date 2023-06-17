package tcp;


import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.packet.Packet;
import xyz.synse.packetnet.packet.PacketBuilder;
import xyz.synse.packetnet.server.Connection;
import xyz.synse.packetnet.server.Server;
import xyz.synse.packetnet.server.listeners.ServerListener;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TCPBenchmarkTest {
    protected static final int tcpPort = 42365, udpPort = 42366;
    protected static final float invMega = 1 / 1000000f;
    protected static final float invNano = 1 / 1000000000f;

    protected Server server;
    protected Client client;
    protected long start;
    protected long end;

    @BeforeEach
    public void setup() throws Exception {
        setLoggingLevel(Level.ERROR);
        server = new Server();
        server.start(tcpPort, udpPort);

        client = new Client();
        client.connect("localhost", tcpPort, udpPort);
    }

    @AfterEach
    public void teardown() throws Exception
    {
        client.close();
        server.waitForEmptyServer();
        server.stop();
    }

    @Test
    public void testEmptyPacketsPerSecond() throws Exception {
        final int amount = 1000;

        final Packet packet = new PacketBuilder().build();

        server.addListener(new ServerListener() {
            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) throws IOException {
                assertTrue(packet.validateHashcode());
            }
        });

        start = System.nanoTime();
        for (int i = 0; i < amount; i++) {
            assertTrue(client.send(packet, ProtocolType.TCP));
        }
        end = System.nanoTime();

        System.out.println(amount / ((end - start) * invNano) + " packets per second");
    }

    @Test
    public void testMBPerSecond() throws Exception {
        final int amount = 1000;

        final byte[] randomData = new byte[4096];
        new Random().nextBytes(randomData);

        final Packet packet = new PacketBuilder()
                .withBytes(randomData)
                .build();

        server.addListener(new ServerListener() {
            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) throws IOException {
                assertTrue(packet.validateHashcode());
            }
        });

        start = System.nanoTime();
        for (int i = 0; i < amount; i++) {
            assertTrue(client.send(packet, ProtocolType.TCP));
        }
        end = System.nanoTime();

        System.out.println((randomData.length * invMega * amount) / ((end - start) * invNano) + " MB per second");
    }

    public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }
}