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

    public static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Utils.class.getName()) && !ste.getClassName().startsWith("java.lang.Thread")) {
                String className = ste.getClassName();
                int lastDotIndex = className.lastIndexOf('.');
                if (lastDotIndex != -1) {
                    return className.substring(lastDotIndex + 1);
                } else {
                    return className;
                }
            }
        }
        return null;
    }

}
