package xyz.synse.packetnet.benchmark;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.*;
import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packet.Packet;
import xyz.synse.packetnet.server.Server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientThroughputTest {
    private static final int WARMUP_DURATION_SECONDS = 5; // Warm-up duration in seconds
    private static final int TEST_DURATION_SECONDS = 10; // Test duration in seconds
    private static final int TCP_PORT = 1234; // TCP port to connect to
    private static final int UDP_PORT = 1235; // UDP port to connect to

    private static Server server;
    private static Client client;

    @BeforeAll
    public void setUp() throws IOException, InterruptedException {
        setLoggingLevel(Level.ERROR);

        server = new Server();
        server.start(TCP_PORT, UDP_PORT);

        client = new Client();
        assertTrue(client.connect("127.0.0.1", TCP_PORT, UDP_PORT));

        System.out.println("Warming up...");
        warmup(ProtocolType.TCP);
        warmup(ProtocolType.UDP);
        System.out.println("Warm-up complete!");
    }

    @Test
    public void testTCPThroughput() throws IOException, InterruptedException {
        testThroughput(ProtocolType.TCP, TEST_DURATION_SECONDS);
    }

    @Test
    public void testUDPThroughput() throws IOException, InterruptedException {
        testThroughput(ProtocolType.UDP, TEST_DURATION_SECONDS);
    }

    private void warmup(ProtocolType protocol) throws IOException, InterruptedException {
        testThroughput(protocol, WARMUP_DURATION_SECONDS);
    }

    private void testThroughput(ProtocolType protocol, int duration) throws IOException, InterruptedException {
        // Calculate the test end time
        Instant endTime = Instant.now().plus(Duration.ofSeconds(duration));

        // Initialize counters
        long packetCount = 0;
        long byteCount = 0;

        // Create a packet
        Packet packet = createPacket(server.getWriteBufferSize());

        // Main throughput test loop
        while (Instant.now().isBefore(endTime)) {
            // Measure throughput
            Instant start = Instant.now();
            assertTrue(client.send(packet, protocol));
            Instant end = Instant.now();

            long durationMillis = Duration.between(start, end).toMillis();
            int packetSize = packet.getBuffer().array().length;

            packetCount++;
            byteCount += packetSize;
        }

        // Calculate throughput metrics
        double throughputMBps = calculateThroughputMBps(byteCount, duration);
        double throughputPPS = calculateThroughputPPS(packetCount, duration);

        // Output results (you can use a proper logging framework)
        System.out.println(protocol + " Throughput (MB/s): " + throughputMBps);
        System.out.println(protocol + " Throughput (PPS): " + throughputPPS);
    }

    // Helper method to calculate throughput in MB/s
    private double calculateThroughputMBps(long byteCount, int testDurationSeconds) {
        return (double) byteCount / (1024 * 1024) / testDurationSeconds;
    }

    // Helper method to calculate throughput in packets per second (PPS)
    private double calculateThroughputPPS(long packetCount, int testDurationSeconds) {
        return (double) packetCount / testDurationSeconds;
    }

    // Replace this with your packet creation logic
    private Packet createPacket(int writeBufferSize) {
        Packet packet = new Packet((short) 123);
        Random random = new Random();
        byte[] randomData = new byte[writeBufferSize - Short.BYTES - Integer.BYTES];
        random.nextBytes(randomData);
        packet.getBuffer().put(randomData);
        packet.getBuffer().setSpace(randomData.length);

        return packet;
    }

    public static void setLoggingLevel(Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }
}
