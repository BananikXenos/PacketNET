package xyz.synse.packetnet.server.listeners;

import xyz.synse.packetnet.common.KDebug;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.server.Connection;

import java.net.Socket;

public class ServerListener {
    public void onConnected(Connection connection) {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Client connected (" + connection.getTcpSocket().getInetAddress() + ":" + connection.getTcpSocket().getPort() + ")");
    }

    public void onDisconnected(Connection connection) {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Client disconnected (" + connection.getTcpSocket().getInetAddress() + ":" + connection.getTcpSocket().getPort() + ")");
    }

    public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Received packet " + packet.getShortString() + " using " + protocolType.name() + " (" + connection.getTcpSocket().getInetAddress() + ":" + connection.getTcpSocket().getPort() + ")");
    }

    public void onSent(Connection connection, ProtocolType protocolType, Packet packet) {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Sent packet " + packet.getShortString() + " using " + protocolType.name() + " to " + connection.getTcpSocket().getInetAddress() + ":" + connection.getTcpSocket().getPort());
    }

    public void onUDPEstablished(Connection connection) {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Established UDP Connection on port " + connection.getUdpPort().get() + " (" + connection.getTcpSocket().getInetAddress() + ":" + connection.getTcpSocket().getPort() + ")");
    }

    public void onStarted() {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Server started.");
    }

    public void onStopped() {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Server stopped.");
    }
}
