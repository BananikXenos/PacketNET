package xyz.synse.packetnet;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.client.listeners.ClientListener;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packet.*;
import xyz.synse.packetnet.server.Connection;
import xyz.synse.packetnet.server.Server;
import xyz.synse.packetnet.server.listeners.ServerListener;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Test {
    public static void main(String[] args) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        // First, for this example, we will disable sfl4j logging just to see our own messages
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);

        // Create a simple server
        Server server = new Server(/*read buffer size (8192)*/ /*write buffer size (8192)*/);
        /*
        Add a listener
        We will add all of them
         */
        server.addListener(new ServerListener() {
            @Override
            public void onConnected(Connection connection, ProtocolType protocolType) {
                System.out.println("[SERVER] " + connection + " connected using " + protocolType.name());
            }

            @Override
            public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) throws IOException {
                System.out.println("[SERVER] Received packet " + packet.toString() + " from " + connection.toString() + " using " + protocolType.name());
                // Now we will read the packet (YOU HAVE TO READ IN THE SAME ORDER AS WRITTEN)
                PacketReader packetReader = new PacketReader(packet);
                String ourString = packetReader.readString();
                long ourTime = packetReader.readLong();
                System.out.println("[SERVER] Read " + ourString + " and " + ourTime);
            }

            @Override
            public void onDisconnected(Connection connection) {
                System.out.println("[SERVER] " + connection.toString() + " disconnected");
            }
        });
        // Start server at port 4443 for TCP and 4444 for UDP
        server.start(4443, 4444);

        // Create a simple client
        Client client = new Client(/*read buffer size (8192)*/ /*write buffer size (8192)*/ /*(should match server!)*/);
        /*
        Add a listener
        Again, we will add all of them
         */
        client.addListener(new ClientListener() {
            @Override
            public void onConnected(ProtocolType protocolType) {
                System.out.println("[CLIENT] Connected to server using " + protocolType.name());
            }

            @Override
            public void onReceived(ProtocolType protocolType, Packet packet) throws IOException {
                System.out.println("[CLIENT] Received packet " + packet.toString() + " using " + protocolType.name());
            }

            @Override
            public void onDisconnected() {
                System.out.println("[CLIENT] Disconnected from server");
            }
        });
        // Join the server (same ports as server)
        client.connect("127.0.0.1", 4443, 4444);

        // We will create a simple packet with id 1, short string and current time as long
        Packet packet = new PacketBuilder()
                .withID((short)1)
                .withString("Hello, World!")
                .withLong(System.currentTimeMillis())
                .build();

        // Optionally, we can compress the packet
        Packet compressedPacket = PacketCompressor.compress(packet);
        // Note: To use this, you have to decompress it first before using it (PacketCompressor.decompress(compressedPacket)) at onReceived

        // Optionally, we can encrypt the packet
        Packet encryptPacket = PacketEncryptor.encrypt(packet, "5rT31^fcs4MpUBPI");
        // Note: Again as before, we have to decrypt it first before using it (PacketEncryptor.decrypt(encryptedPacket, <key used to encrypt>)) at onReceived

        // Finally we can send the packet. We will send it using TCP
        client.send(packet, ProtocolType.TCP);
        
        // We will wait for 1 second
        Thread.sleep(1000L);

        // Disconnect the client from the server
        client.close();
        // Wait for the server to be empty
        server.waitForEmptyServer();
        // Finally, stop the server
        server.stop();
    }
}