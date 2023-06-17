package xyz.synse.packetnet;

import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.client.listeners.ClientListener;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.compression.PacketCompressor;
import xyz.synse.packetnet.common.encryption.PacketEncryptor;
import xyz.synse.packetnet.packet.Packet;
import xyz.synse.packetnet.packet.PacketBuilder;
import xyz.synse.packetnet.packet.PacketReader;
import xyz.synse.packetnet.server.Connection;
import xyz.synse.packetnet.server.Server;
import xyz.synse.packetnet.server.listeners.ServerListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

public class Test {
    private static final String secretKey = "1F16hIQ3SjQ$k1!9";

    public static void main(String[] args) throws Exception {
        // Create and start the server
        Server serverInstance = new Server();
        serverInstance.addListener(new ServerListener() {
            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) throws IOException {
                processPacket(packet);
            }
        });
        serverInstance.start(3300, 3301);

        // Create and connect the client to the server
        Client clientInstance = new Client();
        clientInstance.addListener(new ClientListener());
        clientInstance.connect("127.0.0.1", 3300, 3301);

        // Create a sample packet and encrypt, compress it
        Packet packet = createSamplePacket();
        Packet encryptedPacket = PacketEncryptor.encrypt(packet, secretKey);
        Packet compressedPacket = PacketCompressor.compress(encryptedPacket, PacketCompressor.GZIP_COMPRESSOR);

        // Send the compressed packet using UDP and TCP
        for (int i = 0; i < 1000; i++) {
            clientInstance.send(compressedPacket, ProtocolType.UDP);
            clientInstance.send(compressedPacket, ProtocolType.TCP);
            Thread.sleep(10);
        }

        // Wait for a few seconds
        Thread.sleep(3000L);

        // Broadcast the compressed packet to all connected clients using TCP & UDP
        serverInstance.broadcast(ProtocolType.TCP, compressedPacket);
        serverInstance.broadcast(ProtocolType.UDP, compressedPacket);

        // Wait for a second
        Thread.sleep(1000L);

        // Disconnect the client from the server
        clientInstance.close();

        // Wait for a second
        Thread.sleep(1000L);

        // Stop the server
        serverInstance.stop();
    }

    public static Packet createSamplePacket() {
        Random random = new Random();

        return new PacketBuilder()
                .withID((short) 1)
                .withEnum(TestEnum.TWO)
                .withObject(new TestClass())
                .withInt(random.nextInt())
                .withString("Hello, World!")
                .withBoolean(random.nextBoolean())
                .withDouble(random.nextDouble())
                .withLong(random.nextLong())
                .withFloat(random.nextFloat())
                .withShort((short) random.nextInt(Short.MAX_VALUE))
                .withBytes(new byte[]{0x01, 0x02, 0x03})
                .withUUID(UUID.randomUUID())
                .build();
    }

    private static void processPacket(Packet packet) {
        // Modified packet during delivery
        if(!packet.validateHashcode()) return;

        try {
            Packet decompressedPacket = PacketCompressor.decompress(packet, PacketCompressor.GZIP_COMPRESSOR);
            Packet decryptedPacket = PacketEncryptor.decrypt(decompressedPacket, secretKey);
            try (PacketReader reader = new PacketReader(decryptedPacket)) {
                // Read the values in the same order as they were written and use the values
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static enum TestEnum {
        ONE,
        TWO,
        THREE
    }

    public static class TestClass implements Serializable {
        private final String hello = "Hello";
        private final String world = "World";

        public TestClass() {
        }
    }
}