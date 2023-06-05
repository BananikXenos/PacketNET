package xyz.synse.packetnet.server.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.server.Connection;

import java.net.Socket;

public interface IServerListener {
    void onConnected(Connection connection);
    void onDisconnected(Connection connection);
    void onReceived(Connection connection, ProtocolType protocolType, Packet packet);
    void onSent(Connection connection, ProtocolType protocolType, Packet packet);
}
