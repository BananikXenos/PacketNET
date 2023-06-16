package xyz.synse.packetnet.common.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Checksum {
    public static byte[] calculateChecksum(byte[] inputData) {
        try {
            // Create an instance of the SHA-256 digest algorithm
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Calculate the digest (checksum) of the input data
            return md.digest(inputData);
        } catch (NoSuchAlgorithmException e) {
            // Handle the exception if the SHA-256 algorithm is not available
            e.printStackTrace();
        }

        return null;
    }

    public static String checksumToString(byte[] checksum){
        StringBuilder sb = new StringBuilder();
        for (byte b : checksum) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
