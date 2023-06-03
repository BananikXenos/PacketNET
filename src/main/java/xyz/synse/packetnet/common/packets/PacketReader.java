package xyz.synse.packetnet.common.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketReader implements AutoCloseable {
    private final ByteArrayInputStream byteIn;
    private final DataInputStream in;

    public PacketReader(Packet packet) {
        this.byteIn = new ByteArrayInputStream(packet.getData());
        this.in = new DataInputStream(byteIn);
    }

    public byte[] readBytes() {
        try {
            int size = in.readInt();
            return in.readNBytes(size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readBytes(int len) {
        try {
            return in.readNBytes(len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte readByte(){
        try {
            return in.readByte();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean readBoolean(){
        try {
            return in.readBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readString(){
        try {
            return in.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int readInt(){
        try {
            return in.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long readLong(){
        try {
            return in.readLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public float readFloat(){
        try {
            return in.readFloat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double readDouble(){
        try {
            return in.readDouble();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public short readShort(){
        try {
            return in.readShort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        byteIn.close();
        in.close();
    }
}
