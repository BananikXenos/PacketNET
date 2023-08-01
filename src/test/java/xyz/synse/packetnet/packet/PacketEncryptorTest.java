package xyz.synse.packetnet.packet;

import org.junit.jupiter.api.Test;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packet.Packet;
import xyz.synse.packetnet.common.packet.PacketBuilder;
import xyz.synse.packetnet.common.packet.PacketEncryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class PacketEncryptorTest {
    private static final String key = "1F16hIQ3SjQ$k1!9";
    @Test
    public void runTest() throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int[] packetSizes = {1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576};

        for (int size : packetSizes) {
            // Create the original packet
            byte[] bytes = new byte[size];
            new Random().nextBytes(bytes);
            Packet originalPacket = new PacketBuilder()
                    .withID((short) 2)
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

            // Encrypt the packet
            Packet encryptedPacket = PacketEncryptor.encrypt(originalPacket, key);

            // Decrypt the packet and calculate the hash code
            Packet decryptedPacket = PacketEncryptor.decrypt(encryptedPacket, key);

            // Check
            assertArrayEquals(originalPacket.getData(), decryptedPacket.getData(),
                    "Compression and decompression failed for packet size: " + size);
        }
    }
}
