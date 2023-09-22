package xyz.synse.packetnet.common.data;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicByteBufferEncryptTest {
    private static final String key = "1F16hIQ3SjQ$k1!9";

    @Test
    public void runTest() throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ShortBufferException {
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

            DynamicByteBuffer encrypted = byteBuffer.copy();
            encrypted.encrypt(key);
            encrypted.decrypt(key);

            // Check
            assertArrayEquals(byteBuffer.array(), encrypted.array(), "Encryption and decryption failed for packet size: " + size);
        }
    }
}
