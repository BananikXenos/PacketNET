
# PacketNET

PacketNET is a Java library for network communication using custom packet-based protocols. It provides a simple and efficient way to send and receive packets over different transport protocols.


## Features

- Support for both UDP and TCP protocols
- Packet compression using LZ4 or GZip algorithm
- Server and client implementations
- Event-based listener system
- Easy-to-use API for creating and processing packets
## Installation

To use PacketNET in your Java project, you can download the JAR file from the [releases](https://github.com/BananikXenos/PacketNet/releases) page and add it to your project's dependencies.
## Usage
Check out the [example code](#example) below to see how PacketNET can be used to create a server-client application.
### Example

```java
    // Create and start the server
    Server server = new Server();
    server.addListener(createServerListener());
    server.start(3300, 3301);

    // Create and connect the client to the server
    Client client = new Client();
    client.addListener(createClientListener());
    client.connect("localhost", 3300, 3301);

    // Create a sample packet and compress it
    Packet packet = createSamplePacket();
    Packet compressedPacket = PacketCompressor.compress(packet, PacketCompressor.LZ4_COMPRESSOR);

    // Send the compressed packet using UDP and TCP
    client.send(compressedPacket, ProtocolType.UDP);
    client.send(compressedPacket, ProtocolType.TCP);

    // Wait for a few seconds
    Thread.sleep(3000L);

    // Broadcast the compressed packet to all connected clients using TCP
    server.broadcast(ProtocolType.TCP, compressedPacket);

    // Wait for a second
    Thread.sleep(1000L);

    // Disconnect the client from the server
    client.disconnect();

    // Wait for a second
    Thread.sleep(1000L);

    // Stop the server
    server.stop();
}

public static Packet createSamplePacket() {
    try (PacketBuilder builder = new PacketBuilder((short) 1)) {
        Random random = new Random();

        return builder
                .withInt(random.nextInt())
                .withString(generateRandomString(10))
                .withBoolean(random.nextBoolean())
                .withDouble(random.nextDouble())
                .withLong(random.nextLong())
                .withFloat(random.nextFloat())
                .withShort((short) random.nextInt(Short.MAX_VALUE))
                .build();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

private static void processPacket(Packet packet) {
    Packet decompressedPacket = PacketCompressor.decompress(packet, PacketCompressor.LZ4_COMPRESSOR);
    try (PacketReader reader = new PacketReader(decompressedPacket)) {
        int intValue = reader.readInt();
        String stringValue = reader.readString();
        boolean booleanValue = reader.readBoolean();
        double doubleValue = reader.readDouble();
        long longValue = reader.readLong();
        float floatValue = reader.readFloat();
        short shortValue = reader.readShort();

        System.out.println("Int Value: " + intValue);
        System.out.println("String Value: " + stringValue);
        System.out.println("Boolean Value: " + booleanValue);
        System.out.println("Double Value: " + doubleValue);
        System.out.println("Long Value: " + longValue);
        System.out.println("Float Value: " + floatValue);
        System.out.println("Short Value: " + shortValue);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

private static String generateRandomString(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder(length);
    Random random = new Random();

    for (int i = 0; i < length; i++) {
        int index = random.nextInt(chars.length());
        sb.append(chars.charAt(index));
    }

    return sb.toString();
}

public static IServerListener createServerListener() {
    return new ServerListenerAdapter() {
        @Override
        public void onConnected(Socket socket) {
            System.out.println("SERVER >> Client connected");
        }

        @Override
        public void onDisconnected(Socket socket) {
            System.out.println("SERVER >> Client disconnected");
        }

        @Override
        public void onReceived(InetAddress address, int port, ProtocolType protocolType, Packet packet) {
            System.out.println("SERVER >> Data received from " + address.getHostName() + ":" + port + " using " + protocolType.name() + "(" + packet.getData().length + " bytes" + ")");
            processPacket(packet);
        }

        @Override
        public void onSent(InetAddress address, int port, ProtocolType protocolType, Packet packet) {
            System.out.println("SERVER >> Data sent to " + address.getHostName() + ":" + port + " using " + protocolType.name() + "(" + packet.getData().length + " bytes" + ")");
        }
    };
}

public static IClientListener createClientListener() {
    return new ClientListenerAdapter() {
        @Override
        public void onConnected() {
            System.out.println("CLIENT >> Connected to server");
        }

        @Override
        public void onDisconnected() {
            System.out.println("CLIENT >> Disconnected from server");
        }

        @Override
        public void onReceived(ProtocolType protocolType, Packet packet) {
            System.out.println("CLIENT >> Data received using " + protocolType.name() + "(" + packet.getData().length + " bytes" + ")");
        }

        @Override
        public void onSent(ProtocolType protocolType, Packet packet) {
            System.out.println("CLIENT >> Data sent using " + protocolType.name() + "(" + packet.getData().length + " bytes" + ")");
        }
    };
}
```
## Support

If you find PacketNET useful, you can support the project by:

- [Making a donation via PayPal](https://paypal.me/scgxenos)
- [Buying me a coffee](https://www.buymeacoffee.com/synse)
Your support is greatly appreciated and helps to keep the project active and maintained.
## License

PacketNET is released under the [MIT License](https://choosealicense.com/licenses/mit/)

