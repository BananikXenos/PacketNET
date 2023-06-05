package xyz.synse.packetnet.server.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;
import xyz.synse.packetnet.server.Connection;

public class ServerListenerAdapter implements IServerListener {

    @Override
    public void onConnected(Connection connection) {

    }

    @Override
    public void onDisconnected(Connection connection) {

    }

    @Override
    public void onReceived(Connection connection, ProtocolType protocolType, Packet packet) {

    }

    @Override
    public void onSent(Connection connection, ProtocolType protocolType, Packet packet) {

    }
}
