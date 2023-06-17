package compression;

import org.junit.jupiter.api.Test;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.compression.PacketCompressor;
import xyz.synse.packetnet.packet.Packet;
import xyz.synse.packetnet.packet.PacketBuilder;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class LZ4Test {
    @Test
    public void testLZ4CompressionAndDecompression() throws IOException {
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

            // Compress the packet
            Packet compressedPacket = PacketCompressor.compress(originalPacket, PacketCompressor.LZ4_COMPRESSOR);

            // Decompress the packet and calculate the hash code
            Packet decompressedPacket = PacketCompressor.decompress(compressedPacket, PacketCompressor.LZ4_COMPRESSOR);

            // Check
            assertArrayEquals(originalPacket.getData(), decompressedPacket.getData(),
                    "Compression and decompression failed for packet size: " + size);
        }
    }
}
