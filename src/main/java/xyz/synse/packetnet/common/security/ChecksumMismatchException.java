package xyz.synse.packetnet.common.security;

public class ChecksumMismatchException extends Exception {
    private final String expectedChecksum;
    private final String calculatedChecksum;

    public ChecksumMismatchException(byte[] expectedChecksum, byte[] calculatedChecksum) {
        super("Got a checksum value of " + SHA256Checksum.checksumToString(calculatedChecksum) +", expected " + SHA256Checksum.checksumToString(expectedChecksum));
        this.expectedChecksum = SHA256Checksum.checksumToString(expectedChecksum);
        this.calculatedChecksum = SHA256Checksum.checksumToString(calculatedChecksum);
    }

    public String getCalculatedChecksum() {
        return calculatedChecksum;
    }

    public String getExpectedChecksum() {
        return expectedChecksum;
    }
}
