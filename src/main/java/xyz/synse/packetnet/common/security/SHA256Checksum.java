package xyz.synse.packetnet.common.security;

import xyz.synse.packetnet.common.security.exceptions.ChecksumCalculationException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Checksum {
    public static byte[] calculateChecksum(byte[] inputData) throws ChecksumCalculationException {
        try {
            // Create an instance of the SHA-256 digest algorithm
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Calculate the digest (checksum) of the input data
            return md.digest(inputData);
        } catch (NoSuchAlgorithmException e) {
            throw new ChecksumCalculationException("Failed to calculate checksum: " + e.getMessage());
        }
    }

    public static String checksumToString(byte[] checksum){
        StringBuilder sb = new StringBuilder();
        for (byte b : checksum) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
