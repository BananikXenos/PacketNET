package xyz.synse.packetnet.server;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.server.listeners.IServerListener;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int bufferSize;
    private int tcpPort;
    private int udpPort;
    private boolean running;
    private DatagramSocket udpSocket;
    private ServerSocket tcpSocket;
    private final List<Socket> sockets = new ArrayList<>();
    private final List<IServerListener> listeners = new ArrayList<>();
    private Thread tcpThread;
    private Thread udpThread;

    /**
     * Creates a new instance of the Server class.
     */
    public Server(int bufferSize) {
        this.bufferSize = bufferSize;
        running = false;
    }

    /**
     * Starts the server on the specified TCP and UDP ports.
     *
     * @param tcpPort The TCP port to listen on.
     * @param udpPort The UDP port to listen on.
     */
    public void start(int tcpPort, int udpPort) {
        if (running) {
            System.out.println("Server is already running.");
            return;
        }

        this.tcpPort = tcpPort;
        this.udpPort = udpPort;

        try {
            tcpSocket = new ServerSocket(tcpPort);
            udpSocket = new DatagramSocket(udpPort);

            tcpThread = new Thread(this::startTcpListener);
            udpThread = new Thread(this::startUdpListener);

            tcpThread.start();
            udpThread.start();

            running = true;
            System.out.println("Server started.");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        tcpThread.interrupt();
        udpThread.interrupt();

        tcpSocket.close();
        tcpSocket = null;
        udpSocket.close();
        udpSocket = null;

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

    private void startTcpListener() {
        while (!Thread.interrupted() && tcpSocket != null) {
            try {
                Socket clientSocket = tcpSocket.accept();
                sockets.add(clientSocket);
                listeners.forEach(iServerListener -> iServerListener.onConnected(clientSocket));
                Thread clientThread = new Thread(() -> handleTcpClient(clientSocket));
                clientThread.start();
            } catch (SocketException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startUdpListener() {
        byte[] buffer = new byte[bufferSize];

        while (!Thread.interrupted()) {
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            try {
                if (udpSocket == null) {
                    break; // Exit the loop if udpSocket is null (server is closed)
                }
                udpSocket.receive(datagramPacket);
                byte[] data = datagramPacket.getData();
                int length = datagramPacket.getLength();
                InetAddress address = datagramPacket.getAddress();
                int port = datagramPacket.getPort();

                Packet packet = Packet.fromByteArray(buffer);
                listeners.forEach(iServerListener -> iServerListener.onReceived(address, port, ProtocolType.UDP, packet));
            } catch (SocketException e) {
                // SocketException will be thrown when the socket is closed,
                // so we can safely break the loop in this case
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleTcpClient(Socket clientSocket) {
        try {
            while (!Thread.interrupted()) {
                byte[] buffer = new byte[bufferSize];
                int bytesRead;

                try {
                    bytesRead = clientSocket.getInputStream().read(buffer);
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
                listeners.forEach(iServerListener -> iServerListener.onReceived(clientSocket.getInetAddress(), clientSocket.getPort(), ProtocolType.TCP, packet));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sockets.remove(clientSocket);
        listeners.forEach(iServerListener -> iServerListener.onDisconnected(clientSocket));
    }

    /**
     * Sends data to a specific client using the specified protocol.
     *
     * @param address  The client's IP address.
     * @param port     The client's port.
     * @param packet   The packet to send.
     * @param protocol The protocol to use (TCP or UDP).
     */
    public void send(InetAddress address, int port, Packet packet, ProtocolType protocol) {
        switch (protocol) {
            case TCP -> sendTcp(address, port, packet);
            case UDP -> sendUdp(address, port, packet);
            default -> System.out.println("Unsupported protocol: " + protocol);
        }
    }

    private void sendTcp(InetAddress address, int port, Packet packet) {
        if (tcpSocket != null) {
            for (Socket clientSocket : sockets) {
                if (clientSocket.getInetAddress() != address || clientSocket.getPort() != port) continue;

                try {
                    byte[] data = packet.toByteArray();
                    clientSocket.getOutputStream().write(data);
                    listeners.forEach(iServerListener -> iServerListener.onSent(address, port, ProtocolType.TCP, packet));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void sendUdp(InetAddress address, int port, Packet packet) {
        if (udpSocket != null) {
            try {
                byte[] data = packet.toByteArray();
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length, address, port);
                udpSocket.send(datagramPacket);
                listeners.forEach(iServerListener -> iServerListener.onSent(address, port, ProtocolType.UDP, packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Broadcasts a packet to all connected clients using the specified protocol.
     *
     * @param protocol The protocol to use (TCP or UDP).
     * @param packet   The packet to broadcast.
     */
    public void broadcast(ProtocolType protocol, Packet packet) {
        List<Socket> clientSockets = getSockets();

        for (Socket clientSocket : clientSockets) {
            InetAddress address = clientSocket.getInetAddress();
            int port = clientSocket.getPort();

            send(address, port, packet, protocol);
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
     * Returns the list of connected client sockets.
     *
     * @return The list of sockets.
     */
    public List<Socket> getSockets() {
        return sockets;
    }
}