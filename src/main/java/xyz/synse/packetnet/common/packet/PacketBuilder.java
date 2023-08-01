package xyz.synse.packetnet.common.packet;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

public class PacketBuilder {
    private final Logger logger = LoggerFactory.getLogger(PacketBuilder.class);
    private short packetID;
    private final FastByteArrayOutputStream byteOut;
    private final DataOutputStream out;
    private boolean isBuilt;

    public PacketBuilder() {
        this.byteOut = new FastByteArrayOutputStream();
        this.out = new DataOutputStream(byteOut);
        this.isBuilt = false;
    }

    public synchronized PacketBuilder withID(short id) {
        checkBuilt();
        this.packetID = id;
        return this;
    }

    private void checkBuilt() {
        if (isBuilt) throw new IllegalStateException("Packet already built");
    }

    public synchronized PacketBuilder withBytes(byte[] data) {
        checkBuilt();
        try {
            out.writeInt(data.length);
            out.write(data);
        } catch (final IOException e) {
            logger.error("Unable to add bytes: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withByte(byte b) {
        checkBuilt();
        try {
            out.writeByte(b);
        } catch (final IOException e) {
            logger.error("Unable to add byte: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withBoolean(boolean b) {
        checkBuilt();
        try {
            out.writeBoolean(b);
        } catch (final IOException e) {
            logger.error("Unable to add boolean: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withString(String utf) {
        checkBuilt();
        try {
            byte[] utfBytes = utf.getBytes();
            out.writeInt(utfBytes.length);
            out.write(utfBytes);
        } catch (final IOException e) {
            logger.error("Unable to add String: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withInt(int i) {
        checkBuilt();
        try {
            out.writeInt(i);
        } catch (final IOException e) {
            logger.error("Unable to add int: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withLong(long l) {
        checkBuilt();
        try {
            out.writeLong(l);
        } catch (final IOException e) {
            logger.error("Unable to add long: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withFloat(float f) {
        checkBuilt();
        try {
            out.writeFloat(f);
        } catch (final IOException e) {
            logger.error("Unable to add float: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withDouble(double d) {
        checkBuilt();
        try {
            out.writeDouble(d);
        } catch (final IOException e) {
            logger.error("Unable to add double: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withShort(short sh) {
        checkBuilt();
        try {
            out.writeShort(sh);
        } catch (final IOException e) {
            logger.error("Unable to add short: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withUUID(UUID uuid) {
        checkBuilt();
        try {
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        } catch (final IOException e) {
            logger.error("Unable to add UUID: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withEnum(Enum<?> enu) {
        checkBuilt();
        try {
            out.writeInt(enu.ordinal());
        } catch (final IOException e) {
            logger.error("Unable to add Enum: {} : {}", e.getClass(), e.getMessage());
        }
        return this;
    }

    public synchronized PacketBuilder withObject(Object obj) {
        checkBuilt();

        if (!(obj instanceof Serializable))
            throw new RuntimeException("Object doesn't implement java.io.Serializable");

        try {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {

                objOut.writeObject(obj);
                byte[] bytes = byteOut.toByteArray();
                out.writeInt(bytes.length);
                out.write(bytes);
            }
        } catch (final IOException e) {
            logger.error("Unable to add Object: {} : {}", e.getClass(), e.getMessage());
        }

        return this;
    }

    public synchronized Packet build() {
        checkBuilt();
        isBuilt = true;

        try {
            out.close();
        } catch (final IOException e) {
            logger.error("Unable to build packet: {} : {}", e.getClass(), e.getMessage());
        }

        return new Packet(
                packetID,
                byteOut.array
        );
    }
}
