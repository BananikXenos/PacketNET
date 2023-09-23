
# PacketNET

PacketNET is a Java library for network communication using custom packet-based protocols. It provides a simple and efficient way to send and receive packets over different transport protocols.


## Features

- Support for both TCP and UDP protocols
- Packet compression
- Packet encryption
- Efficient Server and client implementations
- Thread Management
- Event-based listener system
- Easy-to-use API for creating and processing packets
## Installation

To use PacketNET in your Java project, you can download the JAR file from the [releases](https://github.com/BananikXenos/PacketNet/releases) page and add it to your project's dependencies.
## Usage
Check out the [example code](#example) below to see how PacketNET can be used to create a server-client application.
## Example

### Server

#### Creating Server

We can create a Server as following
```java
Server server = new Server(/*read buffer size (8192)*/ /*write buffer size (8192)*/);
```

#### Adding a Server Listener

Listeners can be used for logging and using our packets. We can make a simple listener like this
```java
server.addListener(new ServerListener() {
    @Override
    public void onConnected(Connection connection, ProtocolType protocolType) {
        System.out.println("[SERVER] " + connection + " connected using " + protocolType.name());
    }

    @Override
    public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) throws IOException {
        System.out.println("[SERVER] Received packet " + packet.toString() + " from " + connection.toString() + " using " + protocolType.name());
        // Reading the packet goes here...
    }

    @Override
    public void onDisconnected(Connection connection) {
        System.out.println("[SERVER] " + connection.toString() + " disconnected");
    }
});
```
#### Starting a Server
Starting a Server is simple as doing
```java
server.start(/*tcp port*/4443, /*udp port*/4444);
```
### Client

#### Creating Client

We can create Client as following

```java
Client client = new Client(/*read buffer size (8192)*/ /*write buffer size (8192)*/ /*(should match server!)*/);
```

#### Adding a Client Listener

We can make a simple listener like this
```java
client.addListener(new ClientListener() {
    @Override
    public void onConnected(ProtocolType protocolType) {
        System.out.println("[CLIENT] Connected to server using " + protocolType.name());
    }

    @Override
    public void onReceived(ProtocolType protocolType, Packet packet) throws IOException {
        System.out.println("[CLIENT] Received packet " + packet.toString() + " using " + protocolType.name());
    }

    @Override
    public void onDisconnected() {
        System.out.println("[CLIENT] Disconnected from server");
    }
});
```

#### Connecting to a Server
To connect to a Server we can use

```java
client.connect(/*server ip*/"127.0.0.1", /*server tcp port*/4443, /*server udp port*/4444);
```

### Packets

#### Creating a Packet
Is this example we will make a packet with a short String and a long of our current time in milliseconds
```java
Packet packet = new Packet((short) 1);
packet.getBuffer().putString("Hello, World!");
packet.getBuffer().putLong(System.currentTimeMillis());
```

#### Compressing a Packet (Optional)

This is very simple. Note: You have to decompress the packet before using it `packet.getBuffer().decompress()`, after you have received it using a listener (onReceived)
```java
packet.getBuffer().compress();
```
#### Encrypting a Packet (Optional)
Same goes here. It's very simple, but you have to decrypt it `packet.getBuffer().decrypt(<key used to encrypt>)` before using it, after you have received it using a listener (onReceived).

```java
packet.getBuffer().encrypt("5rT31^fcs4MpUBPI");
```
#### Reading a Packet
You can read the packet like this
```java
String ourString = packet.getBuffer().getString();
long ourTime = packet.getBuffer().getLong();
System.out.println("[SERVER] Read " + ourString + " and " + ourTime);
```

**YOU HAVE TO READ IN THE SAME ORDER AS THE DATA WAS WRITTEN IN**

### Sending a packet
Once you have created your packet, you can send it.
For Client use: 
```java
client.send(packet, ProtocolType.TCP /*or ProtocolType.UDP*/);
```
For Server use:
```java
server.send(connection, packet, ProtocolType.TCP /*or ProtocolType.UDP*/);
```

Both of these functions return a boolean if the sending was successful

### Closing Client/Server
Again, really simple.

For Client use: 
```java
client.close();
```

For Server use:
```java
// Additionally you can wait for the server to be empty.
// server.waitForEmptyServer();
server.close();
```

#### That's it! Have fun using PacketNET!

## Full Example
You can find the full example [here](src/main/java/xyz/synse/packetnet/Example.java)

## Support

If you find PacketNET useful, you can support the project by:

- [Making a donation via PayPal](https://paypal.me/scgxenos)
- [Buying me a coffee](https://www.buymeacoffee.com/synse)
Your support is greatly appreciated and helps to keep the project active and maintained.
## License

PacketNET is released under the [MIT License](https://choosealicense.com/licenses/mit/)

