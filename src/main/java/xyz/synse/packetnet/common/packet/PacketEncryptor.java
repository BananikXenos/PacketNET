package xyz.synse.packetnet.common.packet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class PacketEncryptor {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public static Packet encrypt(Packet packetIn, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        final byte[] data = cipher.doFinal(packetIn.getData());
        return new Packet(packetIn.getID(), data);
    }

    public static Packet decrypt(Packet packetIn, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        final byte[] data = cipher.doFinal(packetIn.getData());
        return new Packet(packetIn.getID(), data);
    }
}
