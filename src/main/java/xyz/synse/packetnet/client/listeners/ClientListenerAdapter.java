package xyz.synse.packetnet.client.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;

public class ClientListenerAdapter implements IClientListener {
    @Override
    public void onReceived(ProtocolType protocolType, Packet packet) {

    }

    @Override
    public void onSent(ProtocolType protocolType, Packet packet) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnected() {

    }
}
