package xyz.synse.packetnet.common.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.UUID;

public class PacketReader implements AutoCloseable {
    private final ByteArrayInputStream byteIn;
    private final DataInputStream in;

    public PacketReader(Packet packet) {
        this.byteIn = new ByteArrayInputStream(packet.getData());
        this.in = new DataInputStream(byteIn);
    }

    public byte[] readBytes() throws IOException {
        int size = in.readInt();
        return in.readNBytes(size);
    }

    public byte[] readBytes(int len) throws IOException {
        return in.readNBytes(len);
    }

    public byte readByte() throws IOException {
        return in.readByte();
    }

    public boolean readBoolean() throws IOException {
        return in.readBoolean();
    }

    public String readString() throws IOException {
        return in.readUTF();
    }

    public int readInt() throws IOException {
        return in.readInt();
    }

    public long readLong() throws IOException {
        return in.readLong();
    }

    public float readFloat() throws IOException {
        return in.readFloat();
    }

    public double readDouble() throws IOException {
        return in.readDouble();
    }

    public short readShort() throws IOException {
        return in.readShort();
    }

    public UUID readUUID() throws IOException {
        long mostSignificantBits = in.readLong();
        long leastSignificantBits = in.readLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    public <T> T readObject() throws IOException, ClassNotFoundException {
        byte[] data = readBytes();
        
        try(
                ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
                ObjectInputStream objIn = new ObjectInputStream(byteIn);
                ){
            Object obj = objIn.readObject();
             
            return ((T) obj);
        }
    }
    
    @Override
    public void close() throws Exception {
        byteIn.close();
        in.close();
    }
}
