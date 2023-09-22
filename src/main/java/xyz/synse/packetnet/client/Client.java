package xyz.synse.packetnet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.synse.packetnet.client.listeners.ClientListener;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packet.Packet;
import xyz.synse.packetnet.common.threading.ThreadPoolManager;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class Client {
    private final Logger logger = LoggerFactory.getLogger(Client.class);
    private ThreadPoolManager threadPoolManager;
    private final int readBufferSize;
    private final int writeBufferSize;

    private final List<ClientListener> listeners = new CopyOnWriteArrayList<>();

    private SocketChannel socketChannel;
    private Socket tcpSocket;
    private DatagramChannel datagramChannel;
    private DatagramSocket udpSocket;
    private CountDownLatch udpConnectionLatch = new CountDownLatch(1);
    private int udpPort;
    private boolean udpConnected = false;

    /**
     * Creates a new instance of the Client class.
     *
     * @param readBufferSize  The size of the buffer for receiving data.
     * @param writeBufferSize The size of the buffer for sending data.
     */
    public Client(int readBufferSize, int writeBufferSize) {
        this.readBufferSize = readBufferSize;
        this.writeBufferSize = writeBufferSize;
    }

    /**
     * Creates a new instance of the Client class with a buffer size of 8192 bytes.
     */
    public Client() {
        this(8192, 8192);
    }

    /**
     * Connects the client to the server using the specified TCP and UDP ports.
     *
     * @param host    The IP address or hostname of the server.
     * @param tcpPort The TCP port of the server.
     * @param udpPort The UDP port of the server.
     * @return True if the client successfully connects, false otherwise.
     */
    public synchronized boolean connect(String host, int tcpPort, int udpPort) {
        if ((this.tcpSocket != null && !this.tcpSocket.isClosed()) || (this.udpSocket != null && !this.udpSocket.isClosed()))
            throw new IllegalArgumentException("Client not closed");
        if (host.isEmpty() || tcpPort == -1 || udpPort == -1)
            throw new IllegalArgumentException("Host and ports are not set");

        logger.info("Connecting to tcp {}:{} & udp {}:{}", host, tcpPort, host, udpPort);

        try {
            setSockets(host, tcpPort, udpPort);
            logger.info("Connected");
            return true;
        } catch (final Exception e) {
            logger.error("Unable to connect: {} :", e.getClass(), e);
            return false;
        }
    }

    /**
     * Sets up the TCP and UDP sockets and launches the listener threads.
     * ex
     *
     * @param host    The IP address or hostname of the server.
     * @param tcpPort The TCP port of the server.
     * @param udpPort The UDP port of the server.
     * @throws IOException if an I/O error occurs while setting up the sockets.
     */
    public synchronized void setSockets(String host, int tcpPort, int udpPort) throws IOException {
        if ((this.tcpSocket != null && !this.tcpSocket.isClosed()) || (this.udpSocket != null && !this.udpSocket.isClosed()))
            throw new IllegalArgumentException("Client not closed");

        InetAddress address = InetAddress.getByName(host);
        this.udpPort = udpPort;

        this.socketChannel = SocketChannel.open(new InetSocketAddress(tcpPort));
        this.tcpSocket = socketChannel.socket();
        this.tcpSocket.setTcpNoDelay(true);
        this.datagramChannel = DatagramChannel.open();
        this.udpSocket = datagramChannel.socket();

        udpSocket.bind(new InetSocketAddress(0));

        threadPoolManager = new ThreadPoolManager();

        threadPoolManager.submit(this::listenerTcpThreadImpl);
        threadPoolManager.submit(this::listenerUdpThreadImpl);

        // Send the UDP port packet to the server
        reconnectUDP();

        // Notify listeners about the connection
        listeners.forEach(listener -> listener.onConnected(ProtocolType.TCP));
    }

    // TCP listener thread implementation
    private void listenerTcpThreadImpl() {
        ByteBuffer buffer = ByteBuffer.allocate(readBufferSize);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int bytesRead = tcpSocket.getInputStream().read(buffer.array());

                if (bytesRead == -1) break;

                buffer.rewind();
                Packet packet = Packet.read(buffer);
                buffer.clear();

                logger.debug("Received packet using TCP: {{}}", packet);

                packetReceived(packet);
            } catch (final SocketException | EOFException e) {
                close();
                break;
            } catch (AsynchronousCloseException e) {
                break;
            } catch (IOException e) {
                logger.error("Error in TCP listener thread: {} :", e.getClass(), e);
                close();
                break;
            }
        }

        logger.debug("TCP listener thread stopped");
    }

    // UDP listener thread implementation
    private void listenerUdpThreadImpl() {
        ByteBuffer buffer = ByteBuffer.allocate(readBufferSize);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(buffer.array(), buffer.capacity());
                udpSocket.receive(datagramPacket);

                buffer.rewind();
                Packet packet = Packet.read(buffer);
                buffer.clear();

                logger.debug("Received packet using UDP: {{}}", packet);

                packetReceived(packet);
            } catch (final SocketException | EOFException e) {
                close();
                break;
            } catch (ClosedByInterruptException e) {
                break;
            } catch (IOException e) {
                logger.error("Error in UDP listener thread: {} :", e.getClass(), e);
                close();
                break;
            }
        }

        logger.debug("UDP listener thread stopped");
    }

    // Process the received packet
    private void packetReceived(Packet packet) {
        boolean handlePacket = postProcessPacket(packet);
        if (!handlePacket) return;

        for (ClientListener listener : listeners) {
            try {
                listener.onReceived(ProtocolType.TCP, packet);
            } catch (final IOException e) {
                logger.warn("Unable to handle Packet: {} :", e.getClass(), e);
            } catch (final Exception e) {
                logger.error("Exception while handling onReceive: {} :", e.getClass(), e);
            }
        }
    }

    // Perform post-processing for the received packet
    private boolean postProcessPacket(Packet packet) {
        if (packet.getID() == (short) -1000) {
            try {
                int udpPort = packet.getBuffer().getInt();

                if (udpPort != udpSocket.getLocalPort()) {
                    logger.error("Invalid UDP port assigned by server. Resending port...");
                    reconnectUDP();
                    return false;
                }

                this.udpConnected = true;
                logger.debug("UDP connection established on port {}", udpPort);
                udpConnectionLatch.countDown();
                listeners.forEach(listener -> listener.onConnected(ProtocolType.UDP));
            } catch (BufferUnderflowException e) {
                logger.error("Unreadable UDP port packet from server. Resending port: {} :", e.getClass(), e);
                reconnectUDP();
            }
            return false;
        }

        return true;
    }

    /**
     * Sends the UDP port packet to the server.
     *
     * @return True if the UDP port packet is sent successfully, false otherwise.
     */
    public synchronized boolean reconnectUDP() {
        if (udpSocket == null || udpSocket.isClosed()) return false;

        udpConnected = false;

        Packet portPacket = new Packet((short) -1000);
        portPacket.getBuffer().putInt(udpSocket.getLocalPort());

        udpConnectionLatch = new CountDownLatch(1);

        boolean sentSuccessfully = send(portPacket, ProtocolType.TCP);

        if (sentSuccessfully) {
            // Wait for the UDP connection to be established
            try {
                udpConnectionLatch.await();
            } catch (InterruptedException e) {
                return false;
            }
        }

        return sentSuccessfully;
    }

    /**
     * Closes the client connection to the server.
     */
    public synchronized void close() {
        if (tcpSocket.isClosed() && udpSocket.isClosed()) return;
        if (tcpSocket == null && udpSocket == null) return;

        logger.info("Closing client");

        try {
            threadPoolManager.shutdown(true);
            logger.debug("Threads interrupted");

            if (tcpSocket != null) {
                tcpSocket.close();
                socketChannel.close();
            }
            if (udpSocket != null) {
                udpConnected = false;
                udpSocket.close();
                datagramChannel.close();
            }
            logger.debug("Sockets closed");

            threadPoolManager.awaitTermination();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
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
     * Sends a packet to the server using the specified protocol.
     *
     * @param packet   The packet to send.
     * @param protocol The protocol to use (TCP or UDP).
     * @return True if the packet is sent successfully, false otherwise.
     */
    public synchronized boolean send(Packet packet, ProtocolType protocol) {
        if (!isConnected(protocol)) return false;

        try {
            switch (protocol) {
                case TCP -> sendTcp(packet);
                case UDP -> sendUdp(packet);
                default -> logger.warn("Unsupported protocol: " + protocol);
            }
            return true;
        } catch (final IOException e) {
            logger.error("Error while sending packet {{}} : {} :", packet, e.getClass(), e);
            return false;
        }
    }

    /**
     * Sends a packet to the server using the TCP protocol.
     *
     * @param packet The packet to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private synchronized void sendTcp(Packet packet) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(writeBufferSize);
        byte[] data = packet.write(buffer).array();

        tcpSocket.getOutputStream().write(data);
        tcpSocket.getOutputStream().flush();
    }

    /**
     * Sends a packet to the server using the UDP protocol.
     *
     * @param packet The packet to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private synchronized void sendUdp(Packet packet) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(writeBufferSize);
        byte[] data = packet.write(buffer).array();

        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, tcpSocket.getInetAddress(), udpPort);
        udpSocket.send(datagramPacket);
    }

    /**
     * Checks if the client is connected using the specified protocol.
     *
     * @param protocolType The protocol to check (TCP or UDP).
     * @return True if the client is connected using the specified protocol, false otherwise.
     */
    public boolean isConnected(ProtocolType protocolType) {
        return switch (protocolType) {
            case TCP -> tcpSocket != null && tcpSocket.isConnected() && !tcpSocket.isClosed();
            case UDP -> udpConnected && udpSocket != null && !udpSocket.isClosed();
        };
    }
}
