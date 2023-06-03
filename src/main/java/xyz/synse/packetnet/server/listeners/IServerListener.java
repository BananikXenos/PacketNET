package xyz.synse.packetnet.server.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;

import java.net.InetAddress;
import java.net.Socket;

public interface IServerListener {
    void onConnected(Socket socket);
    void onDisconnected(Socket socket);
    void onReceived(InetAddress address, int port, ProtocolType protocolType, Packet packet);
    void onSent(InetAddress address, int port, ProtocolType protocolType, Packet packet);
}
