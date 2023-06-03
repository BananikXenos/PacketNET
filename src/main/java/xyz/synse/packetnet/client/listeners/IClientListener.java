package xyz.synse.packetnet.client.listeners;

import xyz.synse.packetnet.client.Client;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;

public interface IClientListener {
    void onReceived(ProtocolType protocolType, Packet packet);
    void onSent(ProtocolType protocolType, Packet packet);

    void onDisconnected();

    void onConnected();
}
