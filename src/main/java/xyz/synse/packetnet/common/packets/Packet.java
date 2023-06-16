package xyz.synse.packetnet.common.packets;

import xyz.synse.packetnet.common.security.ChecksumMismatchException;
import xyz.synse.packetnet.common.security.SHA256Checksum;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
    private final short id;
    private final byte[] data;
    private final byte[] providedChecksum;
    private final byte[] calculatedChecksum;

    public Packet(short id, byte[] data) {
        this.id = id;
        this.data = data;
        this.calculatedChecksum = this.providedChecksum = SHA256Checksum.calculateChecksum(data);
    }

    public Packet(short id, byte[] data, byte[] providedChecksum) {
        this.id = id;
        this.data = data;
        this.providedChecksum = providedChecksum;
        this.calculatedChecksum = SHA256Checksum.calculateChecksum(data);
    }

    public byte[] getData() {
        return data;
    }

    public short getID() {
        return id;
    }

    public byte[] getProvidedChecksum() {
        return providedChecksum;
    }

    public byte[] getCalculatedChecksum() {
        return calculatedChecksum;
    }

    public boolean validateChecksum() {
        return Arrays.equals(calculatedChecksum, providedChecksum);
    }

    public byte[] toByteArray() throws IOException {
        if(calculatedChecksum == null)
            throw new IOException("Invalid checksum calculated");

        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES + Integer.BYTES + calculatedChecksum.length + Integer.BYTES + data.length);

        buffer.putShort(id);

        buffer.putInt(calculatedChecksum.length);
        buffer.put(calculatedChecksum);

        buffer.putInt(data.length);
        buffer.put(data);

        return buffer.array();
    }

    public static Packet fromByteBuffer(ByteBuffer buffer) throws ChecksumMismatchException, IOException {
        // Read packet id
        short id = buffer.getShort();

        // Read packet checksum
        int checksumLen = buffer.getInt();
        byte[] originalChecksum = new byte[checksumLen];
        buffer.get(originalChecksum);

        // Read packet data
        int len = buffer.getInt();
        byte[] data = new byte[len];
        buffer.get(data);

        Packet packet = new Packet(id, data, originalChecksum);

        // Compare checksum
        if (!packet.validateChecksum())
            throw new ChecksumMismatchException(packet.getProvidedChecksum(), packet.getCalculatedChecksum());

        return packet;
    }

    public String getShortString() {
        return String.format("id: %s, data: %d bytes", id, data.length);
    }

    @Override
    public String toString() {
        return "Packet{" +
                "id=" + id +
                ", data=" + Arrays.toString(data) +
                ", checksum=" + SHA256Checksum.checksumToString(providedChecksum) +
                ", calculatedChecksum=" + SHA256Checksum.checksumToString(calculatedChecksum) +
                '}';
    }
}
