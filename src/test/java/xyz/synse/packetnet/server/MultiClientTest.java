package xyz.synse.packetnet.server;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.client.listeners.ClientListener;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packet.Packet;
import xyz.synse.packetnet.server.listeners.ServerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiClientTest {

    private static final int NUM_CLIENTS = 5; // Number of clients to create
    private static final int SERVER_PORT = 4443;
    private static final String SERVER_HOST = "127.0.0.1";

    private static Server server;
    private static List<Client> clients;
    private static CountDownLatch clientsConnectedLatch;

    @BeforeAll
    public static void setUp() {
        // Start the server
        server = new Server();
        server.addListener(new ServerListener() {
            @Override
            public void onConnected(Connection connection, ProtocolType protocolType) {
                System.out.println("[SERVER] " + connection + " connected using " + protocolType.name());
            }

            @Override
            public void onDisconnected(Connection connection) {
                System.out.println("[SERVER] " + connection.toString() + " disconnected");
            }
        });
        server.start(SERVER_PORT, SERVER_PORT);

        // Create and start multiple clients
        clients = new ArrayList<>();
        clientsConnectedLatch = new CountDownLatch(NUM_CLIENTS);

        for (int i = 0; i < NUM_CLIENTS; i++) {
            Client client = getClient();
            clients.add(client);
            client.connect(SERVER_HOST, SERVER_PORT, SERVER_PORT);
        }

        // Wait for all clients to connect
        try {
            assertTrue(clientsConnectedLatch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private static Client getClient() {
        Client client = new Client();
        client.addListener(new ClientListener() {
            @Override
            public void onConnected(ProtocolType protocolType) {
                System.out.println("[CLIENT] Connected to server using " + protocolType.name());
                clientsConnectedLatch.countDown();
            }

            @Override
            public void onDisconnected() {
                System.out.println("[CLIENT] Disconnected from server");
            }
        });
        return client;
    }

    @Test
    public void testMultiClientCommunication() throws InterruptedException {
        // Create a packet to send to the server
        Packet packet = new Packet((short) 1);
        packet.getBuffer().putString("Hello from client!");

        // Send the packet from each client to the server
        for (Client client : clients) {
            client.send(packet, ProtocolType.TCP);
        }

        // Wait for a short time to allow server processing
        Thread.sleep(1000);

        // Assert that the server received a packet from each client
        assertEquals(NUM_CLIENTS, server.getConnections().size());
    }

    @AfterAll
    public static void tearDown() {
        // Close all clients
        for (Client client : clients) {
            client.close();
        }

        // Stop the server
        server.close();
    }
}
