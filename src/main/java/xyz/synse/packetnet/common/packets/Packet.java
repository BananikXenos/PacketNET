package xyz.synse.packetnet.common.packets;

import java.io.*;
import java.nio.ByteBuffer;

public class Packet {
    private final short id;
    private final byte[] data;

    public Packet(short id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public short getID() {
        return id;
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES + Integer.BYTES + data.length);

        buffer.putShort(id);
        buffer.putInt(data.length);
        buffer.put(data);

        return buffer.array();
    }

    public static Packet fromByteBuffer(ByteBuffer buffer) {
        short id = buffer.getShort();
        int len = buffer.getInt();
        byte[] data = new byte[len];
        buffer.get(data);

        return new Packet(id, data);
    }

    public String getShortString() {
        return String.format("id: %s, data: %d bytes", id, data.length);
    }
}
