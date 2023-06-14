package xyz.synse.packetnet;

import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.client.listeners.ClientListenerAdapter;
import xyz.synse.packetnet.client.listeners.IClientListener;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.compression.PacketCompressor;
import xyz.synse.packetnet.common.encryption.PacketEncryptor;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.common.packets.PacketBuilder;
import xyz.synse.packetnet.common.packets.PacketReader;
import xyz.synse.packetnet.server.Connection;
import xyz.synse.packetnet.server.Server;
import xyz.synse.packetnet.server.listeners.IServerListener;
import xyz.synse.packetnet.server.listeners.ServerListenerAdapter;

import java.util.Random;
import java.util.UUID;

public class Test {
    private static final String secretKey = "1F16hIQ3SjQ$k1!9";

    public static void main(String[] args) throws Exception {
        // Create and start the server
        Server serverInstance = new Server();
        serverInstance.addListener(createServerListener());
        serverInstance.start(3300, 3301);

        // Create and connect the client to the server
        Client clientInstance = new Client();
        clientInstance.addListener(createClientListener());
        clientInstance.connect("127.0.0.1", 3300, 3301);

        // Create a sample packet and encrypt, compress it
        Packet packet = createSamplePacket();
        Packet encryptedPacket = PacketEncryptor.encrypt(packet, secretKey);
        Packet compressedPacket = PacketCompressor.compress(encryptedPacket, PacketCompressor.GZIP_COMPRESSOR);

        // Send the compressed packet using UDP and TCP
        clientInstance.send(compressedPacket, ProtocolType.UDP);
        clientInstance.send(compressedPacket, ProtocolType.TCP);

        // Wait for a few seconds
        Thread.sleep(3000L);

        // Broadcast the compressed packet to all connected clients using TCP & UDP
        serverInstance.broadcast(ProtocolType.TCP, compressedPacket);
        serverInstance.broadcast(ProtocolType.UDP, compressedPacket);

        // Wait for a second
        Thread.sleep(1000L);

        // Disconnect the client from the server
        clientInstance.disconnect();

        // Wait for a second
        Thread.sleep(1000L);

        // Stop the server
        serverInstance.stop();
    }

    public static Packet createSamplePacket() {
        try (PacketBuilder builder = new PacketBuilder((short) 1)) {
            Random random = new Random();

            return builder
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void processPacket(Packet packet) throws Exception {
        Packet decompressedPacket = PacketCompressor.decompress(packet, PacketCompressor.GZIP_COMPRESSOR);
        Packet decryptedPacket = PacketEncryptor.decrypt(decompressedPacket, secretKey);
        try (PacketReader reader = new PacketReader(decryptedPacket)) {
            // Read the values in the same order as they were written and use the values
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IServerListener createServerListener() {
        return new ServerListenerAdapter() {
            @Override
            public void onConnected(Connection connection) {
                System.out.println("SERVER >> Client connected");
            }

            @Override
            public void onDisconnected(Connection connection) {
                System.out.println("SERVER >> Client disconnected");
            }

            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) {
                System.out.println("SERVER >> Data received from " + connection.getTcpSocket().getInetAddress() + ":" + (protocolType == ProtocolType.TCP ? connection.getTcpSocket().getPort() : connection.getUdpPort().get()) + " using " + protocolType.name() + "(" + packet.getData().length + " bytes" + ")");
                try {
                    processPacket(packet);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onSent(Connection connection, ProtocolType protocolType, Packet packet) {
                System.out.println("SERVER >> Data sent to " + connection.getTcpSocket().getInetAddress() + ":" + (protocolType == ProtocolType.TCP ? connection.getTcpSocket().getPort() : connection.getUdpPort().get()) + " using " + protocolType.name() + "(" + packet.getData().length + " bytes" + ")");
            }
        };
    }

    public static IClientListener createClientListener() {
        return new ClientListenerAdapter() {
            @Override
            public void onConnected() {
                System.out.println("CLIENT >> Connected to server");
            }

            @Override
            public void onDisconnected() {
                System.out.println("CLIENT >> Disconnected from server");
            }

            @Override
            public void onReceived(ProtocolType protocolType, Packet packet) {
                System.out.println("CLIENT >> Data received using " + protocolType.name() + "(" + packet.getData().length + " bytes" + ")");
            }

            @Override
            public void onSent(ProtocolType protocolType, Packet packet) {
                System.out.println("CLIENT >> Data sent using " + protocolType.name() + "(" + packet.getData().length + " bytes" + ")");
            }
        };
    }
}