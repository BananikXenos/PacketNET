
# PacketNET

PacketNET is a Java library for network communication using custom packet-based protocols. It provides a simple and efficient way to send and receive packets over different transport protocols.


## Features

- Support for both UDP and TCP protocols
- Packet compression using LZ4 or GZip algorithm
- Packet encryption using AES256
- Server and client implementations
- Event-based listener system
- Easy-to-use API for creating and processing packets
## Installation

To use PacketNET in your Java project, you can download the JAR file from the [releases](https://github.com/BananikXenos/PacketNet/releases) page and add it to your project's dependencies.
## Usage
Check out the [example code](#example) below to see how PacketNET can be used to create a server-client application.
### Example

```java
private static final String secretKey = "1F16hIQ3SjQ$k1!9";

public static void main(String[] args) throws Exception {
    // Create and start the server
    Server serverInstance = new Server();
    serverInstance.addListener(new ServerListener() {
        @Override
        public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) throws IOException {
            processPacket(packet);
        }
    });
    serverInstance.start(3300, 3301);

    // Create and connect the client to the server
    Client clientInstance = new Client();
    clientInstance.addListener(new ClientListener());
    clientInstance.connect("127.0.0.1", 3300, 3301);

    // Create a sample packet and encrypt, compress it
    Packet packet = createSamplePacket();
    Packet encryptedPacket = PacketEncryptor.encrypt(packet, secretKey);
    Packet compressedPacket = PacketCompressor.compress(encryptedPacket, PacketCompressor.GZIP_COMPRESSOR);

    // Send the compressed packet using UDP and TCP
    for(int i = 0; i < 1000; i++) {
        clientInstance.send(compressedPacket, ProtocolType.UDP);
        clientInstance.send(compressedPacket, ProtocolType.TCP);
        Thread.sleep(10);
    }

    // Wait for a few seconds
    Thread.sleep(3000L);

    // Broadcast the compressed packet to all connected clients using TCP & UDP
    serverInstance.broadcast(ProtocolType.TCP, compressedPacket);
    serverInstance.broadcast(ProtocolType.UDP, compressedPacket);

    // Wait for a second
    Thread.sleep(1000L);

    // Disconnect the client from the server
    clientInstance.close();

    // Wait for a second
    Thread.sleep(1000L);

    // Stop the server
    serverInstance.stop();
}

public static Packet createSamplePacket() {
    try (PacketBuilder builder = new PacketBuilder((short) 1)) {
        Random random = new Random();

        return builder
                .withEnum(TestEnum.TWO)
                .withObject(new TestClass())
                .withInt(random.nextInt())
                .withString("Hello, World!")
                .withBoolean(random.nextBoolean())
                .withDouble(random.nextDouble())
                .withLong(random.nextLong())
                .withFloat(random.nextFloat())
                .withShort((short) random.nextInt(Short.MAX_VALUE))
                .withBytes(new byte[]{0x01, 0x02, 0x03})
                .withUUID(UUID.randomUUID())
                .build();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

private static void processPacket(Packet packet) {
    try {
        Packet decompressedPacket = PacketCompressor.decompress(packet, PacketCompressor.GZIP_COMPRESSOR);
        Packet decryptedPacket = PacketEncryptor.decrypt(decompressedPacket, secretKey);
        try (PacketReader reader = new PacketReader(decryptedPacket)) {
            // Read the values in the same order as they were written and use the values
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

public static enum TestEnum {
    ONE,
    TWO,
    THREE
}

public static class TestClass implements Serializable {
    private final String hello = "Hello";
    private final String world = "World";

    public TestClass() {
    }
}
```
## Support

If you find PacketNET useful, you can support the project by:

- [Making a donation via PayPal](https://paypal.me/scgxenos)
- [Buying me a coffee](https://www.buymeacoffee.com/synse)
Your support is greatly appreciated and helps to keep the project active and maintained.
## License

PacketNET is released under the [MIT License](https://choosealicense.com/licenses/mit/)

