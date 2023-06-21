package xyz.synse.packetnet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.packet.Packet;
import xyz.synse.packetnet.packet.PacketReader;
import xyz.synse.packetnet.server.listeners.ServerListener;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static xyz.synse.packetnet.threading.ThreadManager.launchThread;

public class Server {
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private final int readBufferSize;
    private final int writeBufferSize;

    private DatagramSocket udpSocket;
    private ServerSocket tcpSocket;

    private final List<Connection> connections = new ArrayList<>();

    private final List<ServerListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Creates a new instance of the Server class.
     *
     * @param readBufferSize  The size of the buffer for receiving data.
     * @param writeBufferSize The size of the buffer for sending data.
     */
    public Server(int readBufferSize, int writeBufferSize) {
        this.readBufferSize = readBufferSize;
        this.writeBufferSize = writeBufferSize;
    }

    /**
     * Creates a new instance of the Server class with a buffer size of 8192 bytes.
     */
    public Server() {
        this(8192, 8192);
    }

    /**
     * Starts the server on the specified TCP and UDP ports.
     *
     * @param tcpPort The TCP port to listen on.
     * @param udpPort The UDP port to listen on.
     * @return true if the server started successfully, false otherwise.
     * @throws IOException if an I/O error occurs while starting the server.
     */
    public synchronized boolean start(int tcpPort, int udpPort) {
        logger.debug("Starting server");

        try {
            tcpSocket = new ServerSocket(tcpPort);
            udpSocket = new DatagramSocket(udpPort);
        } catch (final Exception e) {
            logger.error("Unable to start server: {} :", e.getClass(), e);
            return false;
        }

        logger.debug("Starting threads");
        launchThread(this::acceptorThreadImpl);
        launchThread(this::listenerUdpThreadImpl);

        return true;
    }

    public void waitForEmptyServer() throws InterruptedException {
        do {
            logger.debug("Waiting for server to be empty");
            Thread.sleep(10);
        } while (!connections.isEmpty());
    }

    /**
     * Stops the server and closes all connections.
     */
    public synchronized void stop() {
        logger.info("Stopping server");

        connections.clear();

        try {
            if (tcpSocket != null) tcpSocket.close();
            if (udpSocket != null) udpSocket.close();
            logger.debug("Sockets closed");
        } catch (final Exception e) {
            logger.error("Unable to close server: {} :", e.getClass(), e);
        }
    }

    /**
     * Adds a listener to receive events from the server.
     *
     * @param listener The listener to add.
     */
    public void addListener(ServerListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener from receiving events from the server.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(ServerListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Starts the TCP listener thread to accept incoming connections.
     */
    private void acceptorThreadImpl() {
        while (true) {
            try {
                Socket clientSocket = tcpSocket.accept();
                clientSocket.setTcpNoDelay(true);

                Connection connection = new Connection(clientSocket);
                connections.add(connection);

                listeners.forEach(listener -> listener.onConnected(connection, ProtocolType.TCP));
                launchThread(() -> handleTcpClient(connection));
            } catch (final SocketException ignored) {
                stop();
                break;
            } catch (final IOException e) {
                logger.error("Error in TCP listener thread: {} :", e.getClass(), e);
                stop();
                break;
            }
        }

        logger.debug("TCP listener thread stopped");
    }

    /**
     * Starts the UDP listener thread to receive data from clients.
     */
    private void listenerUdpThreadImpl() {
        ByteBuffer buffer = ByteBuffer.allocate(readBufferSize);

        while (true) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(buffer.array(), buffer.capacity());
                udpSocket.receive(datagramPacket);

                InetAddress address = datagramPacket.getAddress();
                int port = datagramPacket.getPort();

                // Check each connection with the same address and matching UDP port
                Connection connection = connections.stream().filter(conn -> conn.getUdpPort().isPresent() && conn.getTcpSocket().getInetAddress().equals(address) && conn.getUdpPort().get() == port).findFirst().orElse(null);

                if (connection == null) {
                    logger.warn("Packet from unknown connection");
                    continue;
                }

                buffer.rewind();
                Packet packet = Packet.fromByteBuffer(buffer);
                buffer.clear();

                logger.debug("Received packet using UDP from {}:{}: {{}}", connection.getTcpSocket().getInetAddress(), connection.getUdpPort().get(), packet);

                fireReceivedListeners(connection, packet);
            } catch (final SocketException ignored) {
                stop();
                break;
            } catch (final IOException e) {
                logger.error("Error in UDP listener thread: {} :", e.getClass(), e);
                stop();
                break;
            }
        }

        logger.debug("UDP listener thread stopped");
    }


    /**
     * Handles the TCP client connection and processes incoming packets.
     *
     * @param connection The client connection to handle.
     */
    private void handleTcpClient(Connection connection) {
        ByteBuffer buffer = ByteBuffer.allocate(readBufferSize);
        while (true) {
            try {
                int bytesRead = connection.getTcpSocket().getInputStream().read(buffer.array());

                if (bytesRead == -1) {
                    break; // Break the loop when the client disconnects gracefully
                }

                buffer.rewind();
                Packet packet = Packet.fromByteBuffer(buffer);
                buffer.clear();

                logger.debug("Received packet using TCP from {}:{}: {{}}", connection.getTcpSocket().getInetAddress(), connection.getTcpSocket().getPort(), packet);

                boolean handlePacket = postProcessPacket(connection, packet);
                if (!handlePacket) continue;

                fireReceivedListeners(connection, packet);
            } catch (final SocketException ignored) {
                break;
            } catch (final IOException e) {
                logger.error("Error in TCP listener thread: {} :", e.getClass(), e);
                stop();
                break;
            }
        }

        connections.remove(connection);
        listeners.forEach(listener -> listener.onDisconnected(connection));
    }

    private void fireReceivedListeners(Connection connection, Packet packet) {
        for (ServerListener listener : listeners) {
            try {
                listener.onReceived(connection, ProtocolType.TCP, packet);
            } catch (final IOException e) {
                logger.warn("Unable to handle Packet: {} :", e.getClass(), e);
            } catch (final Exception e) {
                logger.error("Exception while handling onReceive: {} :", e.getClass(), e);
            }
        }
    }

    private boolean postProcessPacket(Connection connection, Packet packet) {
        if (packet.getID() == (short) -1000) {
            try {
                PacketReader packetReader = new PacketReader(packet);
                int udpPort = packetReader.readInt();

                connection.setUdpPort(udpPort);
                send(connection, packet, ProtocolType.TCP);
                listeners.forEach(listener -> listener.onConnected(connection, ProtocolType.UDP));
            } catch (Exception e) {
                logger.error("Unreadable UDP port packet from client. {} :", e.getClass(), e);
                connection.removeUdpPort();
            }
            return false;
        }

        return true;
    }

    /**
     * Sends data to a specific client using the specified protocol.
     *
     * @param connection The client's connection object.
     * @param packet     The packet to send.
     * @param protocol   The protocol to use (TCP or UDP).
     */
    public synchronized boolean send(Connection connection, Packet packet, ProtocolType protocol) {
        if (!isClientConnected(connection, protocol)) return false;

        try {
            switch (protocol) {
                case TCP -> sendTcp(connection, packet);
                case UDP -> sendUdp(connection, packet);
                default -> logger.warn("Unsupported protocol: " + protocol);
            }

            return true;
        } catch (final IOException e) {
            logger.error("Error while sending packet {{}} : {} :", packet, e.getClass(), e);
            return false;
        }
    }

    /**
     * Sends a packet to a client using TCP.
     *
     * @param connection The client's connection object.
     * @param packet     The packet to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private synchronized void sendTcp(Connection connection, Packet packet) throws IOException {
        Socket tcpSocket = connection.getTcpSocket();

        byte[] data = packet.toByteBuffer(writeBufferSize).array();

        tcpSocket.getOutputStream().write(data);
        tcpSocket.getOutputStream().flush();
    }

    /**
     * Sends a packet to a client using UDP.
     *
     * @param connection The client's connection object.
     * @param packet     The packet to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private synchronized void sendUdp(Connection connection, Packet packet) throws IOException {
        int udpPort = connection.getUdpPort().get();
        byte[] data = packet.toByteBuffer(writeBufferSize).array();

        DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length, connection.getTcpSocket().getInetAddress(), udpPort);
        udpSocket.send(datagramPacket);
    }

    /**
     * Broadcasts a packet to all connected clients using the specified protocol.
     *
     * @param protocol The protocol to use (TCP or UDP).
     * @param packet   The packet to broadcast.
     * @throws IOException if an I/O error occurs while broadcasting the packet.
     */
    public synchronized void broadcast(ProtocolType protocol, Packet packet) throws IOException {
        for (Connection connection : connections) {
            send(connection, packet, protocol);
        }
    }

    /**
     * Checks if a client connection is connected using the specified protocol.
     *
     * @param connection   The client's connection object.
     * @param protocolType The protocol to check (TCP or UDP).
     * @return True if the client is connected using the specified protocol, false otherwise.
     */
    public synchronized boolean isClientConnected(Connection connection, ProtocolType protocolType) {
        if (protocolType == ProtocolType.TCP)
            return connection.getTcpSocket() != null && connection.getTcpSocket().isConnected();
        else if (protocolType == ProtocolType.UDP)
            return connection.getUdpPort().isPresent();

        return false;
    }

    /**
     * Gets the list of connections to clients.
     *
     * @return The list of connections.
     */
    public List<Connection> getConnections() {
        return connections;
    }
}