package xyz.synse.packetnet.client;

import org.junit.jupiter.api.Test;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packet.Packet;
import xyz.synse.packetnet.server.Connection;
import xyz.synse.packetnet.server.Server;
import xyz.synse.packetnet.server.listeners.ServerListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientTest {
    @Test
    public void runLargeTest() throws IOException, InterruptedException {
        // Create the original packet
        byte[] bytes = new byte[7168];
        new Random().nextBytes(bytes);
        Packet originalPacket = new Packet((short) 2);
        originalPacket.getBuffer().putString("Hello, World!");
        originalPacket.getBuffer().putDouble(69.420D);
        originalPacket.getBuffer().putBoolean(false);
        originalPacket.getBuffer().putLong(System.currentTimeMillis());
        originalPacket.getBuffer().putFloat(842F);
        originalPacket.getBuffer().putString("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        originalPacket.getBuffer().put(bytes);

        // Computed hashes
        final List<Packet> packets = new ArrayList<>();

        // Create server for validation
        Server server = new Server(16384,16384);
        server.addListener(new ServerListener() {
            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) {
                packets.add(packet);
            }
        });
        server.start(3300, 3301);

        // Create client to send the packets
        Client client = new Client(16384,16384);
        client.connect(InetAddress.getLocalHost().getHostName(), 3300, 3301);

        // Send x packets
        for (int i = 0; i < 20; i++) {
            client.send(originalPacket, ProtocolType.TCP);
        }

        // Close connections
        Thread.sleep(1000L);
        client.close();
        server.close();
    }

    @Test
    public void runSmallTest() throws IOException, InterruptedException {
        // Create the original packet
        Packet originalPacket = new Packet((short) 2);
        originalPacket.getBuffer().putString("Hello, World!");

        // Computed hashes
        final List<Packet> packets = new ArrayList<>();

        // Create server for validation
        Server server = new Server();
        server.addListener(new ServerListener() {
            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) {
                packets.add(packet);
            }
        });
        server.start(3300, 3301);

        // Create client to send the packets
        Client client = new Client();
        client.connect(InetAddress.getLocalHost().getHostName(), 3300, 3301);

        // Send x packets
        for (int i = 0; i < 20; i++) {
            client.send(originalPacket, ProtocolType.TCP);
        }

        // Close connections
        Thread.sleep(1000L);
        client.close();
        server.close();

        // Validate hash codes
        for (int i = 0; i < 20; i++) {
            assertNotNull(packets.get(i));
        }
    }
}
