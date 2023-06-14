package xyz.synse.packetnet.common.compression.compressors;

import xyz.synse.packetnet.common.compression.ICompressor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class DeflaterCompressor implements ICompressor {
    @Override
    public byte[] compress(byte[] in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        defl.write(in);
        defl.flush();
        defl.close();

        return out.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InflaterOutputStream infl = new InflaterOutputStream(out);
        infl.write(in);
        infl.flush();
        infl.close();

        return out.toByteArray();
    }
}
