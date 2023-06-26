package xyz.synse.packetnet.server;

import org.junit.jupiter.api.Test;
import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.client.listeners.ClientListener;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.packet.Packet;
import xyz.synse.packetnet.packet.PacketBuilder;
import xyz.synse.packetnet.server.listeners.ServerListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerTest {
    @Test
    public void runLargeTest() throws IOException, InterruptedException {
        // Create the original packet
        byte[] bytes = new byte[7168];
        new Random().nextBytes(bytes);
        Packet originalPacket = new PacketBuilder().withID((short) 2).withString("Hello, World!").withEnum(ProtocolType.TCP).withDouble(69.420D).withBoolean(false).withLong(System.currentTimeMillis()).withFloat(842F).withString("Lorem ipsum dolor sit amet, consectetur adipiscing elit.").withUUID(UUID.randomUUID()).withBytes(bytes).build();

        // Computed hashes
        final List<Packet> packets = new ArrayList<>();

        // Create server for validation
        Server server = new Server();
        server.start(3300, 3301);

        // Create client to send the packets
        Client client = new Client();
        client.addListener(new ClientListener() {
            @Override
            public void onReceived(ProtocolType protocolType, Packet packet) {
                packets.add(packet);
            }
        });
        server.addListener(new ServerListener() {
            @Override
            public void onConnected(Connection connection, ProtocolType protocolType) {
                if(protocolType != ProtocolType.TCP) return;

                try {
                    // Send x packets
                    for (int i = 0; i < 20; i++) {
                        server.broadcast(originalPacket, ProtocolType.TCP);
                    }

                    // Close connections
                    while (packets.size() != 20) {
                        Thread.sleep(1000L);
                    }
                    client.close();
                    server.stop();

                    // Validate hash codes
                    for (int i = 0; i < 20; i++) {
                        assertTrue(packets.get(i).validateHashcode());
                    }
                }catch (Exception ex){}
            }
        });
        client.connect(InetAddress.getLocalHost().getHostName(), 3300, 3301);
    }

    @Test
    public void runSmallTest() throws IOException, InterruptedException {
        // Create the original packet
        Packet originalPacket = new PacketBuilder().withString("Hello, World!").build();

        // Computed hashes
        final List<Packet> packets = new ArrayList<>();

        // Create server for validation
        Server server = new Server();
        server.start(3300, 3301);

        // Create client to send the packets
        Client client = new Client();
        client.addListener(new ClientListener() {
            @Override
            public void onReceived(ProtocolType protocolType, Packet packet) {
                packets.add(packet);
            }
        });
        server.addListener(new ServerListener() {
            @Override
            public void onConnected(Connection connection, ProtocolType protocolType) {
                if(protocolType != ProtocolType.TCP) return;

                try {
                    // Send x packets
                    for (int i = 0; i < 20; i++) {
                        server.broadcast(originalPacket, ProtocolType.TCP);
                    }

                    // Close connections
                    while (packets.size() != 20) {
                        Thread.sleep(1000L);
                    }
                    client.close();
                    server.stop();

                    // Validate hash codes
                    for (int i = 0; i < 20; i++) {
                        assertTrue(packets.get(i).validateHashcode());
                    }
                }catch (Exception ex){}
            }
        });
        client.connect(InetAddress.getLocalHost().getHostName(), 3300, 3301);
    }
}
