package xyz.synse.packetnet.server.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.Utils;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.server.Connection;

public class ServerListener {
    public void onConnected(Connection connection) {
        System.out.printf("[%s] Client connected (%s:%d)%n",
                Utils.getCallerClassName(), connection.getTcpSocket().getInetAddress(),
                connection.getTcpSocket().getPort());
    }

    public void onDisconnected(Connection connection) {
        System.out.printf("[%s] Client disconnected (%s:%d)%n",
                Utils.getCallerClassName(), connection.getTcpSocket().getInetAddress(),
                connection.getTcpSocket().getPort());
    }

    public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) {
        System.out.printf("[%s] Received packet %s using %s (%s:%d)%n",
                Utils.getCallerClassName(), packet.getShortString(), protocolType.name(),
                connection.getTcpSocket().getInetAddress(), connection.getTcpSocket().getPort());
    }

    public void onSent(Connection connection, ProtocolType protocolType, Packet packet) {
        System.out.printf("[%s] Sent packet %s using %s to %s:%d%n",
                Utils.getCallerClassName(), packet.getShortString(), protocolType.name(),
                connection.getTcpSocket().getInetAddress(), connection.getTcpSocket().getPort());
    }

    public void onUDPEstablished(Connection connection) {
        System.out.printf("[%s] Established UDP Connection on port %d (%s:%d)%n",
                Utils.getCallerClassName(), connection.getUdpPort().get(),
                connection.getTcpSocket().getInetAddress(), connection.getTcpSocket().getPort());
    }

    public void onStarted() {
        System.out.printf("[%s] Server started.%n",
                Utils.getCallerClassName());
    }

    public void onStopped() {
        System.out.printf("[%s] Server stopped.%n",
                Utils.getCallerClassName());
    }
}