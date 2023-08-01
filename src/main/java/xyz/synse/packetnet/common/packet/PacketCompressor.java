package xyz.synse.packetnet.common.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PacketCompressor {
    private static final int INFLATE_BUFFER_SIZE = 16;

    public static Packet decompress(final Packet packet) throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
        final GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Read from input until everything is inflated
        final byte[] buffer = new byte[INFLATE_BUFFER_SIZE];
        int bytesInflated;
        while ((bytesInflated = gzipInputStream.read(buffer)) >= 0) {
            byteArrayOutputStream.write(buffer, 0, bytesInflated);
        }

        return new Packet(
                packet.getID(),
                byteArrayOutputStream.toByteArray()
        );
    }

    public static Packet compress(final Packet packet) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);

        gzipOutputStream.write(packet.getData());
        gzipOutputStream.close();

        return new Packet(
                packet.getID(),
                byteArrayOutputStream.toByteArray()
        );
    }
}
