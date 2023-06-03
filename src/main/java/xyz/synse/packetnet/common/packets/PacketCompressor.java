package xyz.synse.packetnet.common.packets;

import xyz.synse.packetnet.common.compression.ICompressor;
import xyz.synse.packetnet.common.compression.compressors.GZipCompressor;
import xyz.synse.packetnet.common.compression.compressors.LZ4Compressor;

public class PacketCompressor {
    public static final ICompressor GZIP_COMPRESSOR = new GZipCompressor();
    public static final ICompressor LZ4_COMPRESSOR = new LZ4Compressor();

    public static Packet decompress(Packet packet, ICompressor compressor) {
        byte[] decompressed = compressor.decompress(packet.getData());
        return new Packet(packet.getID(), decompressed);
    }

    public static Packet compress(Packet packet, ICompressor compressor) {
        byte[] compressed = compressor.compress(packet.getData());
        return new Packet(packet.getID(), compressed);
    }
}
