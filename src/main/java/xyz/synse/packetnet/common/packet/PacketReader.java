package xyz.synse.packetnet.common.packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class PacketReader {
    private final Logger logger = LoggerFactory.getLogger(PacketReader.class);
    private final ByteBuffer byteBuffer;

    public PacketReader(Packet packet) {
        this.byteBuffer = ByteBuffer.wrap(packet.getData());
    }

    public synchronized short readID() {
        return byteBuffer.getShort();
    }

    public synchronized byte[] readBytes() {
        int length = byteBuffer.getInt();
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        return bytes;
    }

    public synchronized byte readByte() {
        return byteBuffer.get();
    }

    public synchronized boolean readBoolean() {
        return byteBuffer.get() != 0;
    }

    public synchronized String readString() {
        int length = byteBuffer.getInt();
        byte[] utfBytes = new byte[length];
        byteBuffer.get(utfBytes);
        return new String(utfBytes);
    }

    public synchronized int readInt() {
        return byteBuffer.getInt();
    }

    public synchronized long readLong() {
        return byteBuffer.getLong();
    }

    public synchronized float readFloat() {
        return byteBuffer.getFloat();
    }

    public synchronized double readDouble() {
        return byteBuffer.getDouble();
    }

    public synchronized short readShort() {
        return byteBuffer.getShort();
    }

    public synchronized UUID readUUID() {
        long mostSignificantBits = byteBuffer.getLong();
        long leastSignificantBits = byteBuffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    public synchronized Enum<?> readEnum(Class<? extends Enum<?>> enumClass) {
        int ordinal = byteBuffer.getInt();
        Enum<?>[] enumConstants = enumClass.getEnumConstants();
        if (ordinal >= 0 && ordinal < enumConstants.length) {
            return enumConstants[ordinal];
        } else {
            logger.warn("Invalid ordinal value for enum class {}: {}", enumClass.getSimpleName(), ordinal);
            return null;
        }
    }

    public synchronized Object readObject() {
        try {
            byte[] bytes = readBytes();
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                 ObjectInputStream objIn = new ObjectInputStream(byteIn)) {

                return objIn.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Unable to read Object: {} : {}", e.getClass(), e.getMessage());
            return null;
        }
    }
}
