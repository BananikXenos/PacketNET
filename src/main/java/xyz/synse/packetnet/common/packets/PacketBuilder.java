package xyz.synse.packetnet.common.packets;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.UUID;

public class PacketBuilder implements AutoCloseable {
    private final short id;
    private final ByteArrayOutputStream byteOut;
    private final DataOutputStream out;

    public PacketBuilder(short id) {
        this.id = id;
        this.byteOut = new ByteArrayOutputStream();
        this.out = new DataOutputStream(byteOut);
    }

    public PacketBuilder withBytes(byte[] data) throws IOException {
        return withBytes(data, true);
    }

    public PacketBuilder withBytes(byte[] data, boolean writeSize) throws IOException {
        if (writeSize)
            out.writeInt(data.length);

        out.write(data);

        return this;
    }

    public PacketBuilder withByte(byte b) throws IOException {
        out.writeByte(b);

        return this;
    }

    public PacketBuilder withBoolean(boolean b) throws IOException {
        out.writeBoolean(b);

        return this;
    }

    public PacketBuilder withString(String utf) throws IOException {
        byte[] bytes = utf.getBytes("UTF-8");
        out.writeInt(bytes.length);
        out.write(bytes);

        return this;
    }

    public PacketBuilder withInt(int i) throws IOException {
        out.writeInt(i);

        return this;
    }

    public PacketBuilder withLong(long l) throws IOException {
        out.writeLong(l);

        return this;
    }

    public PacketBuilder withFloat(float f) throws IOException {
        out.writeFloat(f);

        return this;
    }

    public PacketBuilder withDouble(double d) throws IOException {
        out.writeDouble(d);

        return this;
    }

    public PacketBuilder withShort(short sh) throws IOException {
        out.writeShort(sh);

        return this;
    }

    public PacketBuilder withUUID(UUID uuid) throws IOException {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());

        return this;
    }

    public PacketBuilder withObject(Object obj) throws IOException {
        if (!(obj instanceof Serializable))
            throw new RuntimeException("Object doesn't implement java.io.Serializable");

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(obj);
            byte[] bytes = byteOut.toByteArray();
            return withBytes(bytes);
        }
    }

    public Packet build() {
        byte[] data = byteOut.toByteArray();
        return new Packet(id, data);
    }

    @Override
    public void close() throws Exception {
        out.close();
        byteOut.close();
    }
}
