package xyz.synse.packetnet.client;

import xyz.synse.packetnet.client.listeners.IClientListener;
import xyz.synse.packetnet.common.Constants;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private volatile boolean udpListenerRunning;
    private InetAddress serverAddress;
    private int tcpPort;
    private int udpPort;
    private final List<IClientListener> listeners = new ArrayList<>();
    private Socket tcpSocket;
    private DatagramSocket udpSocket;

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

        // Init tcp
        tcpSocket = new Socket(serverAddress, tcpPort);
        Thread tcpThread = new Thread(this::startTcpListener);
        tcpThread.start();

        udpListenerRunning = true;
        // Init udp
        udpSocket = new DatagramSocket();
        Thread udpThread = new Thread(this::startUdpListener);
        udpThread.start();

        // Notify listeners about the connection
        listeners.forEach(IClientListener::onConnected);
    }

    /**
     * Disconnects the client from the server.
     */
    public void disconnect() {
        if (tcpSocket != null) {
            try (Socket socket = tcpSocket) {
                // No need to manually set tcpSocket to null
            } catch (IOException e) {
                e.printStackTrace();
            }
            tcpSocket = null;
        }

        if (udpSocket != null) {
            udpSocket.close();
            udpListenerRunning = false;
            udpSocket = null;
        }

        // Notify listeners about the disconnection
        listeners.forEach(IClientListener::onDisconnected);
    }

    /**
     * Adds a listener to receive events from the client.
     *
     * @param listener The listener to add.
     */
    public void addListener(IClientListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener from receiving events from the client.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(IClientListener listener) {
        this.listeners.remove(listener);
    }

    private void startTcpListener() {
        try {
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = tcpSocket.getInputStream().read(buffer)) != -1) {
                Packet packet = Packet.fromByteArray(buffer);
                listeners.forEach(iClientListener -> iClientListener.onReceived(ProtocolType.TCP, packet));
            }
        } catch (SocketException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startUdpListener() {
        byte[] buffer = new byte[Constants.BUFFER_SIZE];

        while (udpListenerRunning && udpSocket != null && !Thread.interrupted()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                udpSocket.receive(packet);
                byte[] data = packet.getData();

                Packet constructedPacket = Packet.fromByteArray(data);
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
     */
    public void send(Packet packet, ProtocolType protocol) {
        switch (protocol) {
            case TCP -> sendTcp(packet);
            case UDP -> sendUdp(packet);
            default -> System.out.println("Unsupported protocol: " + protocol);
        }
    }

    private void sendTcp(Packet packet) {
        if (tcpSocket != null) {
            try {
                byte[] data = packet.toByteArray();
                tcpSocket.getOutputStream().write(data);

                listeners.forEach(listener -> listener.onSent(ProtocolType.TCP, packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendUdp(Packet packet) {
        if (udpSocket != null) {
            try {
                byte[] data = packet.toByteArray();
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length, serverAddress, udpPort);
                udpSocket.send(datagramPacket);

                listeners.forEach(listener -> listener.onSent(ProtocolType.UDP, packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
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