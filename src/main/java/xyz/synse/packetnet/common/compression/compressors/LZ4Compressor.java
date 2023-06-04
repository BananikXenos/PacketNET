package xyz.synse.packetnet.common.compression.compressors;

import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import xyz.synse.packetnet.common.compression.ICompressor;

import java.io.*;

public class LZ4Compressor implements ICompressor {
    @Override
    public byte[] compress(byte[] data) throws IOException {
        try (
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream dataOut = new DataOutputStream(byteOut);
        ) {
            dataOut.writeInt(data.length);

            try (
                    LZ4FrameOutputStream outStream = new LZ4FrameOutputStream(byteOut);
            ) {
                outStream.write(data);
            }

            return byteOut.toByteArray();
        }
    }

    @Override
    public byte[] decompress(byte[] data) throws IOException {
        try (
                ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
                DataInputStream dataIn = new DataInputStream(byteIn);
        ) {
            int decompressedSize = dataIn.readInt();

            try (
                    LZ4FrameInputStream outStream = new LZ4FrameInputStream(byteIn);
            ) {
                byte[] restored = new byte[decompressedSize];
                outStream.read(restored);
                return restored;
            }
        }
    }
}
