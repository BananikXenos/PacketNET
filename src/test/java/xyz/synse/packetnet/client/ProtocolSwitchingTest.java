package xyz.synse.packetnet.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.client.listeners.ClientListener;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packet.Packet;
import xyz.synse.packetnet.server.Connection;
import xyz.synse.packetnet.server.Server;
import xyz.synse.packetnet.server.listeners.ServerListener;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProtocolSwitchingTest {

    private static final int SERVER_PORT_TCP = 4443;
    private static final int SERVER_PORT_UDP = 4444;
    private static final String SERVER_HOST = "127.0.0.1";

    private static Server server;
    private static Client client;
    private static ProtocolType connectionLatchType = ProtocolType.TCP;
    private static CountDownLatch clientConnectedLatch;
    private static ArrayList<Packet> packetsReceived = new ArrayList<>();

    @BeforeAll
    public static void setUp() throws IOException {
        // Start the server with TCP and UDP listeners
        server = new Server();
        server.addListener(new ServerListener() {

            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) throws IOException {
                if (packet.getID() == (short) 1)
                    packetsReceived.add(packet);
            }

            @Override
            public void onConnected(Connection connection, ProtocolType protocolType) {
                System.out.println("[SERVER] " + connection + " connected using " + protocolType.name());
            }

            @Override
            public void onDisconnected(Connection connection) {
                System.out.println("[SERVER] " + connection.toString() + " disconnected");
            }
        });
        server.start(SERVER_PORT_TCP, SERVER_PORT_UDP);

        // Create a client
        client = new Client();
        client.addListener(new ClientListener() {
            @Override
            public void onConnected(ProtocolType protocolType) {
                System.out.println("[CLIENT] Connected to server using " + protocolType.name());
                if (protocolType == connectionLatchType)
                    clientConnectedLatch.countDown();
            }

            @Override
            public void onDisconnected() {
                System.out.println("[CLIENT] Disconnected from server");
            }
        });
    }

    @Test
    public void testProtocolSwitching() throws InterruptedException {
        // Connect the client using TCP
        connectionLatchType = ProtocolType.TCP;
        clientConnectedLatch = new CountDownLatch(1);
        client.connect(SERVER_HOST, SERVER_PORT_TCP, SERVER_PORT_UDP);

        // Wait for the client to connect
        try {
            assertTrue(clientConnectedLatch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create a packet to send to the server
        Packet packet = new Packet((short) 1);
        packet.getBuffer().putString("Hello from client!");

        // Send the packet using TCP
        client.send(packet, ProtocolType.TCP);

        // Wait for a short time to allow server processing
        Thread.sleep(1000);

        // Disconnect the client from TCP
        client.close();

        // Connect the client using UDP
        connectionLatchType = ProtocolType.UDP;
        clientConnectedLatch = new CountDownLatch(1);
        client.connect(SERVER_HOST, SERVER_PORT_TCP, SERVER_PORT_UDP);

        // Wait for the client to connect
        try {
            assertTrue(clientConnectedLatch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Send the same packet using UDP
        client.send(packet, ProtocolType.UDP);

        // Wait for a short time to allow server processing
        Thread.sleep(1000);

        // Assert that the server received two packets, one via TCP and one via UDP
        assertEquals(2, packetsReceived.size());
    }

    @AfterAll
    public static void tearDown() {
        // Close the client
        client.close();

        // Stop the server
        server.close();
    }
}
