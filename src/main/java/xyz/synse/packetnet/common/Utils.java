package xyz.synse.packetnet.common;

public class Utils {
    public static byte[] expandByteArray(byte[] originalArray, int newSize) {
        if (newSize <= originalArray.length) {
            return originalArray;
        }

        byte[] expandedArray = new byte[newSize];
        System.arraycopy(originalArray, 0, expandedArray, 0, originalArray.length);
        return expandedArray;
    }
}
