package xyz.synse.packetnet.common.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public PacketBuilder withBytes(byte[] data) {
        return withBytes(data, true);
    }

    public PacketBuilder withBytes(byte[] data, boolean writeSize) {
        try {
            if (writeSize)
                out.writeInt(data.length);

            out.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withByte(byte b) {
        try {
            out.writeByte(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withBoolean(boolean b) {
        try {
            out.writeBoolean(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withString(String utf) {
        try {
            out.writeUTF(utf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withInt(int i) {
        try {
            out.writeInt(i);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withLong(long l) {
        try {
            out.writeLong(l);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withFloat(float f) {
        try {
            out.writeFloat(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withDouble(double d) {
        try {
            out.writeDouble(d);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withShort(short sh) {
        try {
            out.writeShort(sh);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public PacketBuilder withUUID(UUID uuid) {
        try {
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public Packet build() {
        try {
            byte[] data = byteOut.toByteArray();

            return new Packet(id, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        out.close();
        byteOut.close();
    }
}
