package xyz.synse.packetnet.common.packet;

import xyz.synse.packetnet.common.data.DynamicByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
    private final short id;
    private final DynamicByteBuffer buffer;

    public Packet(short id, DynamicByteBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    public Packet(short id) {
        this.id = id;
        this.buffer = new DynamicByteBuffer(16, 1.5f);
    }

    public DynamicByteBuffer getBuffer() {
        return buffer;
    }

    public short getID() {
        return id;
    }

    public ByteBuffer write(ByteBuffer outBuffer) {
        // Write packet id
        outBuffer.putShort(id);
        // Read packet data
        outBuffer.putInt(buffer.capacity());
        outBuffer.put(buffer.array());

        return outBuffer;
    }

    public static Packet read(ByteBuffer inBuffer) throws IOException {
        // Read packet id
        short id = inBuffer.getShort();
        // Read packet data length
        int len = inBuffer.getInt();

        // Create a DynamicByteBuffer and read data directly from inBuffer
        DynamicByteBuffer byteBuffer = new DynamicByteBuffer(len, 1.5f);
        byteBuffer.put(inBuffer.array(), inBuffer.position(), len);
        inBuffer.position(inBuffer.position() + len);
        byteBuffer.rewind();

        return new Packet(id, byteBuffer);
    }


    @Override
    public String toString() {
        return String.format("id: %s, data: %d bytes", id, buffer.position());
    }
}
