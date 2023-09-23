package xyz.synse.packetnet.common.data;

import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DynamicByteBuffer implements Comparable<ByteBuffer> {

    private ByteBuffer byteBuffer;
    private float expandFactor;

    public DynamicByteBuffer(int initialCapacity, float expandFactor) {
        if (expandFactor < 1) {
            throw new IllegalArgumentException(
                    "The expand factor must be greater or equal to 1!");
        }
        this.byteBuffer = ByteBuffer.allocate(initialCapacity);
        this.expandFactor = expandFactor;
    }

    public DynamicByteBuffer(int initialCapacity) {
        this(initialCapacity, 2);
    }

    public int capacity() {
        return byteBuffer.capacity();
    }

    public void clear() {
        byteBuffer.clear();
    }

    public Buffer flip() {
        return byteBuffer.flip();
    }

    public boolean hasRemaining() {
        return byteBuffer.hasRemaining();
    }

    public boolean isReadOnly() {
        return byteBuffer.isReadOnly();
    }

    public int limit() {
        return byteBuffer.limit();
    }

    public Buffer limit(int newLimit) {
        return byteBuffer.limit(newLimit);
    }

    public Buffer mark() {
        return byteBuffer.mark();
    }

    public int position() {
        return byteBuffer.position();
    }

    public Buffer position(int newPosition) {
        return byteBuffer.position(newPosition);
    }

    public int remaining() {
        return byteBuffer.remaining();
    }

    public Buffer reset() {
        return byteBuffer.reset();
    }

    public Buffer rewind() {
        return byteBuffer.rewind();
    }

    public byte[] array() {
        return byteBuffer.array();
    }

    public int arrayOffset() {
        return byteBuffer.arrayOffset();
    }

    public ByteBuffer compact() {
        return byteBuffer.compact();
    }

    public int compareTo(@NotNull ByteBuffer that) {
        return byteBuffer.compareTo(that);
    }

    public ByteBuffer duplicate() {
        return byteBuffer.duplicate();
    }

    public boolean equals(Object ob) {
        return byteBuffer.equals(ob);
    }

    public byte get() {
        return byteBuffer.get();
    }

    public ByteBuffer get(byte[] dst) {
        return byteBuffer.get(dst);
    }

    public ByteBuffer get(byte[] dst, int offset, int length) {
        return byteBuffer.get(dst, offset, length);
    }

    public byte get(int index) {
        return byteBuffer.get(index);
    }

    public char getChar() {
        return byteBuffer.getChar();
    }

    public char getChar(int index) {
        return byteBuffer.getChar(index);
    }

    public boolean getBoolean() {
        return byteBuffer.get() == 1;
    }

    public boolean getBoolean(int index) {
        return byteBuffer.get(index) == 1;
    }

    public String getString() {
        int len = byteBuffer.getInt();
        byte[] data = new byte[len];
        byteBuffer.get(data);

        return new String(data, StandardCharsets.UTF_8);
    }

    public String getString(int index) {
        int len = byteBuffer.getInt(index);
        byte[] data = new byte[len];
        byteBuffer.get(index + Integer.BYTES, data);

        return new String(data, StandardCharsets.UTF_8);
    }

    public UUID getUUID() {
        long mostSigBits = getLong();
        long leastSigBits = getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    public UUID getUUID(int index) {
        long mostSigBits = getLong(index);
        long leastSigBits = getLong(index + Long.BYTES);
        return new UUID(mostSigBits, leastSigBits);
    }

    public double getDouble() {
        return byteBuffer.getDouble();
    }

    public double getDouble(int index) {
        return byteBuffer.getDouble(index);
    }

    public float getFloat() {
        return byteBuffer.getFloat();
    }

    public float getFloat(int index) {
        return byteBuffer.getFloat(index);
    }

    public int getInt() {
        return byteBuffer.getInt();
    }

    public int getInt(int index) {
        return byteBuffer.getInt(index);
    }

    public long getLong() {
        return byteBuffer.getLong();
    }

    public long getLong(int index) {
        return byteBuffer.getLong(index);
    }

    public short getShort() {
        return byteBuffer.getShort();
    }

    public short getShort(int index) {
        return byteBuffer.getShort(index);
    }

    public boolean hasArray() {
        return byteBuffer.hasArray();
    }

    public boolean isDirect() {
        return byteBuffer.isDirect();
    }

    public ByteOrder order() {
        return byteBuffer.order();
    }

    public ByteBuffer order(ByteOrder bo) {
        return byteBuffer.order(bo);
    }

    public ByteBuffer put(byte b) {
        ensureSpace(1);
        return byteBuffer.put(b);
    }

    public ByteBuffer put(byte[] src) {
        ensureSpace(src.length);
        return byteBuffer.put(src);
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        ensureSpace(length);
        return byteBuffer.put(src, offset, length);
    }

    public ByteBuffer put(ByteBuffer src) {
        ensureSpace(src.remaining());
        return byteBuffer.put(src);
    }

    public ByteBuffer put(int index, byte b) {
        ensureSpace(1);
        return byteBuffer.put(index, b);
    }

    public ByteBuffer putChar(char value) {
        ensureSpace(2);
        return byteBuffer.putChar(value);
    }

    public ByteBuffer putChar(int index, char value) {
        ensureSpace(2);
        return byteBuffer.putChar(index, value);
    }

    public ByteBuffer putBoolean(boolean value) {
        ensureSpace(1);
        return byteBuffer.put((byte) (value ? 1 : 0));
    }

    public ByteBuffer putBoolean(int index, boolean value) {
        ensureSpace(1);
        return byteBuffer.put(index, (byte) (value ? 1 : 0));
    }

    public ByteBuffer putString(String value) {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        ensureSpace(data.length + Integer.BYTES);

        byteBuffer.putInt(data.length);
        return byteBuffer.put(data);
    }

    public ByteBuffer putString(int index, String value) {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        ensureSpace(data.length + Integer.BYTES);

        byteBuffer.putInt(index, data.length);
        return byteBuffer.put(index + Integer.BYTES, data);
    }

    public ByteBuffer putUUID(UUID value) {
        ensureSpace(2 * Long.BYTES);
        byteBuffer.putLong(value.getMostSignificantBits());
        return putLong(value.getLeastSignificantBits());
    }

    public ByteBuffer putUUID(int index, UUID value) {
        ensureSpace(2 * Long.BYTES);
        byteBuffer.putLong(index, value.getMostSignificantBits());
        return putLong(index + Long.BYTES, value.getLeastSignificantBits());
    }

    public ByteBuffer putDouble(double value) {
        ensureSpace(8);
        return byteBuffer.putDouble(value);
    }

    public ByteBuffer putDouble(int index, double value) {
        ensureSpace(8);
        return byteBuffer.putDouble(index, value);
    }

    public ByteBuffer putFloat(float value) {
        ensureSpace(4);
        return byteBuffer.putFloat(value);
    }

    public ByteBuffer putFloat(int index, float value) {
        ensureSpace(4);
        return byteBuffer.putFloat(index, value);
    }

    public ByteBuffer putInt(int value) {
        ensureSpace(4);
        return byteBuffer.putInt(value);
    }

    public ByteBuffer putInt(int index, int value) {
        ensureSpace(4);
        return byteBuffer.putInt(index, value);
    }

    public ByteBuffer putLong(int index, long value) {
        ensureSpace(8);
        return byteBuffer.putLong(index, value);
    }

    public ByteBuffer putLong(long value) {
        ensureSpace(8);
        return byteBuffer.putLong(value);
    }

    public ByteBuffer putShort(int index, short value) {
        ensureSpace(2);
        return byteBuffer.putShort(index, value);
    }

    public ByteBuffer putShort(short value) {
        ensureSpace(2);
        return byteBuffer.putShort(value);
    }

    public ByteBuffer compress() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);

        gzipOutputStream.write(byteBuffer.array());
        gzipOutputStream.close();

        byte[] compressedData = byteArrayOutputStream.toByteArray();
        setSpace(compressedData.length);

        // Clear the existing data in the original ByteBuffer and put the compressed data
        byteBuffer.clear();
        return byteBuffer.put(compressedData);
    }

    public ByteBuffer decompress() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
        final GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Read from input until everything is inflated
        final byte[] buffer = new byte[16];
        int bytesInflated;
        while ((bytesInflated = gzipInputStream.read(buffer)) >= 0) {
            byteArrayOutputStream.write(buffer, 0, bytesInflated);
        }

        byte[] decompressedData = byteArrayOutputStream.toByteArray();
        setSpace(decompressedData.length);

        // Clear the existing data in the original ByteBuffer and put the decompressed data
        byteBuffer.clear();
        return byteBuffer.put(decompressedData);
    }

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public DynamicByteBuffer encrypt(String key) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return encrypt(new SecretKeySpec(key.getBytes(), ALGORITHM));
    }

    public DynamicByteBuffer encrypt(Key secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        try {
            // Create a Cipher instance
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(byteBuffer.array());
            setSpace(encryptedData.length);

            // Clear the existing data in the original ByteBuffer and put the encrypted data
            byteBuffer.clear();
            byteBuffer.put(encryptedData);

            return this;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw e;
        }
    }

    public DynamicByteBuffer decrypt(String key) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return decrypt(new SecretKeySpec(key.getBytes(), ALGORITHM));
    }

    public DynamicByteBuffer decrypt(Key secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        try {
            // Create a Cipher instance
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(byteBuffer.array());
            setSpace(decryptedData.length);

            // Clear the existing data in the original ByteBuffer and put the decrypted data
            byteBuffer.clear();
            byteBuffer.put(decryptedData);

            return this;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw e;
        }
    }

    public ByteBuffer slice() {
        return byteBuffer.slice();
    }

    public DynamicByteBuffer copy() {
        DynamicByteBuffer copyBuffer = new DynamicByteBuffer(byteBuffer.capacity(), expandFactor);
        copyBuffer.put(byteBuffer.array(), 0, byteBuffer.capacity());
        copyBuffer.position(position());
        return copyBuffer;
    }

    @Override
    public int hashCode() {
        return byteBuffer.hashCode();
    }

    @Override
    public String toString() {
        return byteBuffer.toString();
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setExpandFactor(float expandFactor) {
        if (expandFactor < 1) {
            throw new IllegalArgumentException(
                    "The expand factor must be greater or equal to 1!");
        }
        this.expandFactor = expandFactor;
    }

    public float getExpandFactor() {
        return expandFactor;
    }

    public void ensureSpace(int needed) {
        if (remaining() >= needed) {
            return;
        }

        int currentPosition = byteBuffer.position();
        int newCapacity = (int) (byteBuffer.capacity() * expandFactor);
        while (newCapacity < (currentPosition + needed)) {
            newCapacity = (int) (newCapacity * expandFactor);
        }

        ByteBuffer expanded = ByteBuffer.allocate(newCapacity);
        expanded.order(byteBuffer.order());

        // Rewind the original buffer and copy its data to the expanded buffer
        byteBuffer.rewind();
        expanded.put(byteBuffer);

        // Restore the original position in the expanded buffer
        expanded.position(currentPosition);

        byteBuffer = expanded;
    }

    public void setSpace(int newCapacity) {
        ByteBuffer expanded = ByteBuffer.allocate(newCapacity);
        expanded.order(byteBuffer.order());
        expanded.put(Arrays.copyOf(byteBuffer.array(), newCapacity));
        byteBuffer = expanded;
    }
}