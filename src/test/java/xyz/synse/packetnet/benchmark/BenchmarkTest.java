package xyz.synse.packetnet.benchmark;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BenchmarkTest {
    protected static final int tcpPort = 42365, udpPort = 42366;
    protected Server server;
    protected Client client;

    @BeforeEach
    public void setup() {
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
    public void testMBPSTCP() throws Exception {
        final int amount = 100_000;

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

        client.waitForUDPConnection();

        long start = System.currentTimeMillis();
        for (int i = 0; i < amount; i++) {
            assertTrue(client.send(packet, ProtocolType.TCP));
        }

        System.out.println("TCP: " + ((randomData.length * amount) / 1024f / 1024f) / ((System.currentTimeMillis() - start) / 1000f) + " MB per second");
    }

    @Test
    public void testMBPSUDP() throws Exception {
        final int amount = 100_000;

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

        client.waitForUDPConnection();

        long start = System.currentTimeMillis();
        for (int i = 0; i < amount; i++) {
            assertTrue(client.send(packet, ProtocolType.UDP));
        }

        System.out.println("UDP: " + ((randomData.length * amount) / 1024f / 1024f) / ((System.currentTimeMillis() - start) / 1000f) + " MB per second");
    }

    public static void setLoggingLevel(Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }
}
