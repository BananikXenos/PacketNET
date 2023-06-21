package xyz.synse.packetnet.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
    private final short id;
    private final byte[] data;
    private final int providedHashcode;
    private final int hashcode;

    public Packet(short id, byte[] data) {
        this.id = id;
        this.data = data;

        this.hashcode = Arrays.hashCode(data);
        this.providedHashcode = Arrays.hashCode(data);
    }

    public Packet(short id, byte[] data, int providedHashcode) {
        this.id = id;
        this.data = data;

        this.providedHashcode = providedHashcode;
        this.hashcode = Arrays.hashCode(data);
    }

    public byte[] getData() {
        return data;
    }

    public short getID() {
        return id;
    }

    public int getHashcode() {
        return hashcode;
    }

    public int getProvidedHashcode() {
        return providedHashcode;
    }

    public boolean validateHashcode() {
        return hashcode == providedHashcode;
    }

    public ByteBuffer toByteBuffer(int maxSize) {
        ByteBuffer buffer = ByteBuffer.allocate(maxSize);

        buffer.putShort(id);

        buffer.putInt(hashcode);

        buffer.putInt(data.length);
        buffer.put(data);

        return buffer;
    }

    public static Packet fromByteBuffer(ByteBuffer buffer) throws IOException {
        // Read packet id
        short id = buffer.getShort();

        // Read packet checksum
        int hashcode = buffer.getInt();

        // Read packet data
        int len = buffer.getInt();
        byte[] data = new byte[len];
        buffer.get(data);

        return new Packet(id, data, hashcode);
    }

    @Override
    public String toString() {
        return String.format("id: %s, data: %d bytes", id, data.length);
    }
}
