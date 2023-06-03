package xyz.synse.packetnet.common.packets;

import java.io.*;

public class Packet {
    private final short id;
    private final byte[] data;

    Packet(short id, byte[] data) {
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
        try (
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(byteOut)
        ) {
            out.writeShort(id);
            out.writeInt(data.length);
            out.write(data);

            return byteOut.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Packet fromByteArray(byte[] packet) {
        try (
                ByteArrayInputStream byteIn = new ByteArrayInputStream(packet);
                DataInputStream dataIn = new DataInputStream(byteIn);
        ) {
            short id = dataIn.readShort();
            int len = dataIn.readInt();
            byte[] data = dataIn.readNBytes(len);

            return new Packet(id, data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
