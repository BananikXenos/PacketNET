package xyz.synse.packetnet.client;

import xyz.synse.packetnet.client.listeners.ClientListener;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.common.packets.PacketBuilder;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private final int bufferSize;
    private InetAddress serverAddress;
    private int tcpPort;
    private int udpPort;
    private final List<ClientListener> listeners = new ArrayList<>();
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private Thread tcpThread;
    private Thread udpThread;

    /**
     * Creates a new instance of the Client class.
     *
     * @param bufferSize The size of the buffer for receiving data.
     */
    public Client(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Creates a new instance of the Client class with a buffer size of 8192 bytes.
     */
    public Client() {
        this(8192);
    }

    /**
     * Connects the client to the server using the specified TCP and UDP ports.
     *
     * @param serverAddress The IP address or hostname of the server.
     * @param tcpPort       The TCP port of the server.
     * @param udpPort       The UDP port of the server.
     * @throws IOException if an I/O error occurs during the connection.
     */
    public void connect(String serverAddress, int tcpPort, int udpPort) throws IOException {
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;

        // Initialize TCP connection
        tcpSocket = new Socket(serverAddress, tcpPort);
        tcpThread = new Thread(this::startTcpListener);
        tcpThread.start();

        // Initialize UDP connection
        udpSocket = new DatagramSocket();
        udpThread = new Thread(this::startUdpListener);
        udpThread.start();

        // Send the UDP port packet to the server
        sendUdpPortPacket(udpSocket.getLocalPort());

        // Notify listeners about the connection
        listeners.forEach(ClientListener::onConnected);
    }

    /**
     * Sends the UDP port packet to the server.
     *
     * @param port The local UDP port to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private void sendUdpPortPacket(int port) throws IOException {
        try (PacketBuilder packetBuilder = new PacketBuilder((short) -1000)) {
            packetBuilder.withInt(port);
            sendInternal(packetBuilder.build(), ProtocolType.TCP);
        } catch (Exception e) {
            throw new IOException("Failed sending the UDP port packet", e);
        }
    }

    /**
     * Disconnects the client from the server.
     *
     * @throws IOException if an I/O error occurs during disconnection.
     */
    public void disconnect() throws IOException {
        if (tcpSocket != null && !tcpSocket.isClosed()) {
            tcpSocket.close();
            tcpSocket = null;
        }

        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
            udpSocket = null;
        }

        if (tcpThread != null) {
            tcpThread.interrupt();
            try {
                tcpThread.join();
            } catch (InterruptedException e) {
                // Handle the interrupted exception if required
            }
            tcpThread = null;
        }

        if (udpThread != null) {
            udpThread.interrupt();
            try {
                udpThread.join();
            } catch (InterruptedException e) {
                // Handle the interrupted exception if required
            }
            udpThread = null;
        }

        // Notify listeners about the disconnection
        listeners.forEach(ClientListener::onDisconnected);
    }

    /**
     * Adds a listener to receive events from the client.
     *
     * @param listener The listener to add.
     */
    public void addListener(ClientListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener from receiving events from the client.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(ClientListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Starts the TCP listener thread to receive data from the server.
     */
    private void startTcpListener() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            while (tcpSocket.getInputStream().read(buffer.array()) != -1) {
                buffer.rewind();

                Packet packet = Packet.fromByteBuffer(buffer);
                buffer.clear();

                listeners.forEach(iClientListener -> iClientListener.onReceived(ProtocolType.TCP, packet));
            }
        } catch (SocketException ignored) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the UDP listener thread to receive data from the server.
     */
    private void startUdpListener() {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        while (udpSocket != null && !Thread.interrupted()) {
            buffer.clear();

            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity());
            try {
                udpSocket.receive(packet);
                buffer.rewind();

                Packet constructedPacket = Packet.fromByteBuffer(buffer);
                listeners.forEach(iClientListener -> iClientListener.onReceived(ProtocolType.UDP, constructedPacket));
            } catch (SocketException ignored) {

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a packet to the server using the specified protocol.
     *
     * @param packet   The packet to send.
     * @param protocol The protocol to use (TCP or UDP).
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    public void send(Packet packet, ProtocolType protocol) throws IOException {
        sendInternal(packet, protocol);
        listeners.forEach(listener -> listener.onSent(protocol, packet));
    }

    /**
     * Sends a packet to the server using the specified protocol.
     *
     * @param packet   The packet to send.
     * @param protocol The protocol to use (TCP or UDP).
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private void sendInternal(Packet packet, ProtocolType protocol) throws IOException {
        switch (protocol) {
            case TCP -> sendInternalTcp(packet);
            case UDP -> sendInternalUdp(packet);
            default -> System.out.println("Unsupported protocol: " + protocol);
        }
    }

    /**
     * Sends a packet to the server using the TCP protocol.
     *
     * @param packet The packet to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private void sendInternalTcp(Packet packet) throws IOException {
        if (tcpSocket != null) {
            byte[] data = packet.toByteArray();
            tcpSocket.getOutputStream().write(data);
        }
    }

    /**
     * Sends a packet to the server using the UDP protocol.
     *
     * @param packet The packet to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private void sendInternalUdp(Packet packet) throws IOException {
        if (udpSocket != null) {
            byte[] data = packet.toByteArray();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, serverAddress, udpPort);
            udpSocket.send(datagramPacket);
        }
    }

    /**
     * Returns the TCP port of the server.
     *
     * @return The TCP port.
     */
    public int getTcpPort() {
        return tcpPort;
    }

    /**
     * Returns the UDP port of the server.
     *
     * @return The UDP port.
     */
    public int getUdpPort() {
        return udpPort;
    }

    /**
     * Returns the IP address of the server.
     *
     * @return The server IP address.
     */
    public InetAddress getServerAddress() {
        return serverAddress;
    }
}