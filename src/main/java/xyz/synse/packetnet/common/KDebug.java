package xyz.synse.packetnet.common;

public class KDebug {
    public static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(KDebug.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                return ste.getClassName();
            }
        }
        return null;
    }

    public static String getCallerClassShortName() {
        String fullName = getCallerClassName();
        String[] split = fullName.split("\\.");
        return split[split.length - 1];
    }
}