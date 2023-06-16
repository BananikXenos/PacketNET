package xyz.synse.packetnet.common.compression;

import xyz.synse.packetnet.common.compression.compressors.DeflaterCompressor;
import xyz.synse.packetnet.common.compression.compressors.GZipCompressor;
import xyz.synse.packetnet.common.compression.compressors.LZ4Compressor;
import xyz.synse.packetnet.packet.Packet;
import xyz.synse.packetnet.common.checksum.exceptions.ChecksumCalculationException;

import java.io.IOException;

public class PacketCompressor {
    public static final ICompressor DEFLATER_COMPRESSOR = new DeflaterCompressor();
    public static final ICompressor GZIP_COMPRESSOR = new GZipCompressor();
    public static final ICompressor LZ4_COMPRESSOR = new LZ4Compressor();

    public static Packet decompress(Packet packet, ICompressor compressor) throws IOException, ChecksumCalculationException {
        byte[] decompressed = compressor.decompress(packet.getData());
        return new Packet(packet.getID(), decompressed);
    }

    public static Packet compress(Packet packet, ICompressor compressor) throws IOException, ChecksumCalculationException {
        byte[] compressed = compressor.compress(packet.getData());
        return new Packet(packet.getID(), compressed);
    }
}
