package xyz.synse.packetnet.common.compression;

import xyz.synse.packetnet.common.compression.compressors.DeflaterCompressor;
import xyz.synse.packetnet.common.compression.compressors.GZipCompressor;
import xyz.synse.packetnet.common.compression.compressors.LZ4Compressor;
import xyz.synse.packetnet.common.packets.Packet;

import java.io.IOException;

public class PacketCompressor {
    public static final ICompressor DEFLATER_COMPRESSOR = new DeflaterCompressor();
    public static final ICompressor GZIP_COMPRESSOR = new GZipCompressor();
    public static final ICompressor LZ4_COMPRESSOR = new LZ4Compressor();

    public static Packet decompress(Packet packet, ICompressor compressor) throws IOException {
        byte[] decompressed = compressor.decompress(packet.getData());
        return new Packet(packet.getID(), decompressed);
    }

    public static Packet compress(Packet packet, ICompressor compressor) throws IOException {
        byte[] compressed = compressor.compress(packet.getData());
        return new Packet(packet.getID(), compressed);
    }
}
