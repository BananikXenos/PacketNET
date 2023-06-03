package xyz.synse.packetnet.common.compression;

public interface ICompressor {
    byte[] compress(byte[] data);
    byte[] decompress(byte[] data);
}
