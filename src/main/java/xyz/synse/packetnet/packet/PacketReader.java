package xyz.synse.packetnet.packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;

public class PacketReader implements AutoCloseable {
    private final ByteArrayInputStream byteIn;
    private final DataInputStream in;

    public PacketReader(Packet packet) {
        this.byteIn = new ByteArrayInputStream(packet.getData());
        this.in = new DataInputStream(byteIn);
    }

    public synchronized byte[] readBytes() throws IOException {
        int size = in.readInt();
        return in.readNBytes(size);
    }

    public synchronized byte[] readBytes(int len) throws IOException {
        return in.readNBytes(len);
    }

    public synchronized byte readByte() throws IOException {
        return in.readByte();
    }

    public synchronized boolean readBoolean() throws IOException {
        return in.readBoolean();
    }

    public synchronized String readString() throws IOException {
        return in.readUTF();
    }

    public synchronized int readInt() throws IOException {
        return in.readInt();
    }

    public synchronized long readLong() throws IOException {
        return in.readLong();
    }

    public synchronized float readFloat() throws IOException {
        return in.readFloat();
    }

    public synchronized double readDouble() throws IOException {
        return in.readDouble();
    }

    public synchronized short readShort() throws IOException {
        return in.readShort();
    }

    public synchronized UUID readUUID() throws IOException {
        long mostSignificantBits = in.readLong();
        long leastSignificantBits = in.readLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    public synchronized <T> T readObject() throws IOException, ClassNotFoundException {
        byte[] data = readBytes();
        
        try(
                ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
                ObjectInputStream objIn = new ObjectInputStream(byteIn);
                ){
            Object obj = objIn.readObject();

            @SuppressWarnings("unchecked")
            T result = (T) obj;
            return result;
        }
    }

    public synchronized <T extends Enum<?>> T readEnum(Class<T> enumClass) throws IOException {
        int ordinal = in.readInt();
        T[] enumConstants = enumClass.getEnumConstants();
        if (ordinal >= 0 && ordinal < enumConstants.length) {
            return enumConstants[ordinal];
        } else {
            throw new IllegalArgumentException("Invalid ordinal value for enum " + enumClass.getName());
        }
    }


    @Override
    public synchronized void close() throws IOException {
        byteIn.close();
        in.close();
    }
}
