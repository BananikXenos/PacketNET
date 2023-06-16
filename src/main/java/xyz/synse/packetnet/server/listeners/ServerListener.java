package xyz.synse.packetnet.server.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.packet.Packet;
import xyz.synse.packetnet.server.Connection;

import java.io.IOException;

public class ServerListener {
    public void onConnected(Connection connection, ProtocolType protocolType) {
    }

    public void onDisconnected(Connection connection) {
    }

    public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) throws IOException {
    }
}