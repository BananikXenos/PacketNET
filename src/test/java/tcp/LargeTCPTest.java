package tcp;

import org.junit.jupiter.api.Test;
import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.common.packets.PacketBuilder;
import xyz.synse.packetnet.common.security.exceptions.ChecksumCalculationException;
import xyz.synse.packetnet.server.Connection;
import xyz.synse.packetnet.server.Server;
import xyz.synse.packetnet.server.listeners.ServerListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LargeTCPTest {
    @Test
    public void runTest() throws IOException, InterruptedException, ChecksumCalculationException {
        // Create the original packet
        PacketBuilder packetBuilder = new PacketBuilder((short) 2);
        byte[] bytes = new byte[7168];
        new Random().nextBytes(bytes);
        Packet originalPacket = packetBuilder
                .withString("Hello, World!")
                .withEnum(ProtocolType.TCP)
                .withDouble(69.420D)
                .withBoolean(false)
                .withLong(System.currentTimeMillis())
                .withFloat(842F)
                .withString("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .withUUID(UUID.randomUUID())
                .withBytes(bytes)
                .build();
        packetBuilder.close();

        // Pre-compute original hash code
        int originalHash = Arrays.hashCode(originalPacket.getData());

        // Computed hashes
        final List<Integer> hashes = new ArrayList<>();

        // Create server for validation
        Server server = new Server();
        server.addListener(new ServerListener() {
            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) {
                hashes.add(Arrays.hashCode(packet.getData()));
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
        client.disconnect();
        server.stop();

        // Validate hash codes
        for (int i = 0; i < 20; i++) {
            assertTrue(i < hashes.size());

            int hash = hashes.get(i);
            boolean isValid = hash == originalHash;

            assertTrue(isValid);
        }
    }
}
