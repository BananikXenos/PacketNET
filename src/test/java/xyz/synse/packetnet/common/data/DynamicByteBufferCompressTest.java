package xyz.synse.packetnet.common.data;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicByteBufferCompressTest {
    @Test
    public void runTest() throws IOException, DataFormatException {
        int[] packetSizes = {1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576};

        for (int size : packetSizes) {
            // Create the original packet
            byte[] bytes = new byte[7168];
            new Random().nextBytes(bytes);
            DynamicByteBuffer byteBuffer = new DynamicByteBuffer(16, 1.5f);
            byteBuffer.putString("Hello, World!");
            byteBuffer.putDouble(69.420D);
            byteBuffer.putBoolean(false);
            byteBuffer.putLong(System.currentTimeMillis());
            byteBuffer.putFloat(842F);
            byteBuffer.putString("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
            byteBuffer.put(bytes);
            
            DynamicByteBuffer compressed = byteBuffer.copy();
            compressed.compress();
            compressed.decompress();

            // Check
            assertArrayEquals(byteBuffer.array(), compressed.array(), "Compression and decompression failed for packet size: " + size);
        }
    }
}