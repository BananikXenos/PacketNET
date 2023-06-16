package xyz.synse.packetnet.client.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.packet.Packet;

import java.io.IOException;

public class ClientListener {
    public void onReceived(ProtocolType protocolType, Packet packet) throws IOException {
    }

    public void onDisconnected() {
    }

    public void onConnected(ProtocolType protocolType) {
    }
}