package xyz.synse.packetnet.common.packets;

import xyz.synse.packetnet.common.objects.Tuple;
import xyz.synse.packetnet.common.security.exceptions.ChecksumCalculationException;
import xyz.synse.packetnet.common.security.exceptions.ChecksumException;
import xyz.synse.packetnet.common.security.exceptions.ChecksumMismatchException;
import xyz.synse.packetnet.common.security.SHA256Checksum;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
    private final short id;
    private final byte[] data;
    private final Tuple<byte[], byte[]> checksums = new Tuple<>();

    public Packet(short id, byte[] data) throws ChecksumCalculationException {
        this.id = id;
        this.data = data;

        byte[] calculatedChecksum = SHA256Checksum.calculateChecksum(data);
        this.checksums.setA(calculatedChecksum);
        this.checksums.setB(calculatedChecksum);
    }

    public Packet(short id, byte[] data, byte[] providedChecksum) throws ChecksumCalculationException {
        this.id = id;
        this.data = data;

        this.checksums.setA(providedChecksum);
        this.checksums.setB(SHA256Checksum.calculateChecksum(data));
    }

    public byte[] getData() {
        return data;
    }

    public short getID() {
        return id;
    }

    public byte[] getProvidedChecksum() {
        return this.checksums.getA();
    }

    public byte[] getCalculatedChecksum() {
        return this.checksums.getB();
    }

    public boolean validateChecksum() {
        return Arrays.equals(getCalculatedChecksum(), getProvidedChecksum());
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES + Integer.BYTES + getCalculatedChecksum().length + Integer.BYTES + data.length);

        buffer.putShort(id);

        buffer.putInt(getCalculatedChecksum().length);
        buffer.put(getCalculatedChecksum());

        buffer.putInt(data.length);
        buffer.put(data);

        return buffer.array();
    }

    public static Packet fromByteBuffer(ByteBuffer buffer) throws ChecksumException, IOException {
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
                ", checksum=" + SHA256Checksum.checksumToString(getProvidedChecksum()) +
                ", calculatedChecksum=" + SHA256Checksum.checksumToString(getCalculatedChecksum()) +
                '}';
    }
}
