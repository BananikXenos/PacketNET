package xyz.synse.packetnet.common.compression;

import java.io.IOException;

public interface ICompressor {
    byte[] compress(byte[] data) throws IOException;
    byte[] decompress(byte[] data) throws IOException;
}
