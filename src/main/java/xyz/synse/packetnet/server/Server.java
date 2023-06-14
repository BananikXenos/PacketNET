package xyz.synse.packetnet.server;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.common.packets.PacketReader;
import xyz.synse.packetnet.server.listeners.IServerListener;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int bufferSize;
    private int tcpPort;
    private int udpPort;
    private boolean running;
    private DatagramSocket udpSocket;
    private ServerSocket tcpSocket;
    private final Map<InetAddress, Connection> connections = new HashMap<>();
    private final List<IServerListener> listeners = new ArrayList<>();
    private ExecutorService executorService;

    /**
     * Creates a new instance of the Server class.
     *
     * @param bufferSize The size of the buffer for receiving data.
     */
    public Server(int bufferSize) {
        this.bufferSize = bufferSize;
        running = false;
    }

    /**
     * Creates a new instance of the Server class with buffer size of 8192 bytes.
     */
    public Server() {
        this(8192);
    }

    /**
     * Starts the server on the specified TCP and UDP ports.
     *
     * @param tcpPort The TCP port to listen on.
     * @param udpPort The UDP port to listen on.
     * @throws IOException if an I/O error occurs while starting the server.
     */
    public void start(int tcpPort, int udpPort) throws IOException {
        if (running) {
            System.out.println("Server is already running.");
            return;
        }

        this.tcpPort = tcpPort;
        this.udpPort = udpPort;

        executorService = Executors.newCachedThreadPool();

        tcpSocket = new ServerSocket(tcpPort);
        udpSocket = new DatagramSocket(udpPort);

        executorService.execute(this::startTcpListener);
        executorService.execute(this::startUdpListener);

        running = true;
        System.out.println("Server started.");
    }

    /**
     * Stops the server and closes all connections.
     *
     * @throws IOException if an I/O error occurs while closing the server socket.
     */
    public void stop() throws IOException {
        if (!running) {
            System.out.println("Server is not running.");
            return;
        }

        executorService.shutdownNow();

        tcpSocket.close();
        udpSocket.close();

        connections.clear();

        running = false;
        System.out.println("Server stopped.");
    }

    /**
     * Adds a listener to receive events from the server.
     *
     * @param listener The listener to add.
     */
    public void addListener(IServerListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener from receiving events from the server.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(IServerListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Starts the TCP listener thread to accept incoming connections.
     */
    private void startTcpListener() {
        while (!Thread.interrupted()) {
            try {
                Socket clientSocket = tcpSocket.accept();
                Connection connection = new Connection(clientSocket);
                connections.put(clientSocket.getInetAddress(), connection);
                listeners.forEach(listener -> listener.onConnected(connection));
                executorService.submit(() -> handleTcpClient(connection));
            } catch (SocketException ignored) {
                // SocketException will be thrown when the socket is closed,
                // so we can safely break the loop in this case
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the UDP listener thread to receive data from clients.
     */
    private void startUdpListener() {
        byte[] buffer = new byte[bufferSize];

        while (!Thread.interrupted()) {
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            try {
                udpSocket.receive(datagramPacket);
                InetAddress address = datagramPacket.getAddress();
                int port = datagramPacket.getPort();

                Connection connection = connections.get(address);
                if (connection == null || !connection.getUdpPort().isPresent() || connection.getUdpPort().get() != port) {
                    System.err.println("Packet from unknown connection");
                    continue;
                }

                Packet packet = Packet.fromByteArray(datagramPacket.getData());
                listeners.forEach(listener -> listener.onReceived(connection, ProtocolType.UDP, packet));
            } catch (SocketException e) {
                // SocketException will be thrown when the socket is closed,
                // so we can safely break the loop in this case
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the TCP client connection and processes incoming packets.
     *
     * @param connection The client connection to handle.
     */
    private void handleTcpClient(Connection connection) {
        try {
            while (!Thread.interrupted()) {
                byte[] buffer = new byte[bufferSize];
                int bytesRead;

                try {
                    bytesRead = connection.getTcpSocket().getInputStream().read(buffer);
                } catch (SocketException e) {
                    // Handle disconnection scenario separately
                    if (e.getMessage().equals("Socket closed")) {
                        break; // Break the loop and proceed to clean up
                    } else {
                        throw e; // Re-throw for other SocketExceptions
                    }
                }

                if (bytesRead == -1) {
                    break; // Break the loop when the client disconnects gracefully
                }

                Packet packet = Packet.fromByteArray(buffer);
                if (packet.getID() == (short) -1000) {
                    try (PacketReader packetReader = new PacketReader(packet)) {
                        int udpPort = packetReader.readInt();

                        connection.setUdpPort(Optional.of(udpPort));
                        System.out.println("Established UDP Connection");
                    } catch (Exception ignored) {
                        System.err.println("Malformed udp port packet");
                    }
                    continue;
                }

                listeners.forEach(listener -> listener.onReceived(connection, ProtocolType.TCP, packet));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        connections.remove(connection.getTcpSocket().getInetAddress());
        listeners.forEach(listener -> listener.onDisconnected(connection));
    }

    /**
     * Sends data to a specific client using the specified protocol.
     *
     * @param connection The client's connection object.
     * @param packet     The packet to send.
     * @param protocol   The protocol to use (TCP or UDP).
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    public void send(Connection connection, Packet packet, ProtocolType protocol) throws IOException {
        switch (protocol) {
            case TCP:
                sendTcp(connection, packet);
                break;
            case UDP:
                sendUdp(connection, packet);
                break;
            default:
                System.out.println("Unsupported protocol: " + protocol);
        }
    }

    /**
     * Sends a packet to a client using TCP.
     *
     * @param connection The client's connection object.
     * @param packet     The packet to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private void sendTcp(Connection connection, Packet packet) throws IOException {
        Socket tcpSocket = connection.getTcpSocket();
        if (tcpSocket != null && tcpSocket.isConnected()) {
            byte[] data = packet.toByteArray();
            tcpSocket.getOutputStream().write(data);
            listeners.forEach(listener -> listener.onSent(connection, ProtocolType.TCP, packet));
        }
    }

    /**
     * Sends a packet to a client using UDP.
     *
     * @param connection The client's connection object.
     * @param packet     The packet to send.
     * @throws IOException if an I/O error occurs while sending the packet.
     */
    private void sendUdp(Connection connection, Packet packet) throws IOException {
        Optional<Integer> udpPortOptional = connection.getUdpPort();
        if (udpPortOptional.isEmpty()) {
            throw new IOException("No UDP connection established");
        }

        int udpPort = udpPortOptional.get();
        if (udpSocket != null) {
            byte[] data = packet.toByteArray();
            DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length, connection.getTcpSocket().getInetAddress(), udpPort);
            udpSocket.send(datagramPacket);
            listeners.forEach(listener -> listener.onSent(connection, ProtocolType.UDP, packet));
        }
    }

    /**
     * Broadcasts a packet to all connected clients using the specified protocol.
     *
     * @param protocol The protocol to use (TCP or UDP).
     * @param packet   The packet to broadcast.
     * @throws IOException if an I/O error occurs while broadcasting the packet.
     */
    public void broadcast(ProtocolType protocol, Packet packet) throws IOException {
        for (Connection connection : connections.values()) {
            if (protocol == ProtocolType.UDP && !connection.getUdpPort().isPresent())
                continue;

            send(connection, packet, protocol);
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
     * Returns the list of connected client connections.
     *
     * @return The list of connections.
     */
    public Collection<Connection> getConnections() {
        return connections.values();
    }
}