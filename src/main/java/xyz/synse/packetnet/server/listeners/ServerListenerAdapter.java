package xyz.synse.packetnet.server.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;

import java.net.InetAddress;
import java.net.Socket;

public class ServerListenerAdapter implements IServerListener {
    @Override
    public void onConnected(Socket socket) {

    }

    @Override
    public void onDisconnected(Socket socket) {

    }

    @Override
    public void onReceived(InetAddress address, int port, ProtocolType protocolType, Packet packet) {

    }

    @Override
    public void onSent(InetAddress address, int port, ProtocolType protocolType, Packet packet) {

    }
}
