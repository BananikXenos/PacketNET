package xyz.synse.packetnet.client.listeners;

import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.Utils;
import xyz.synse.packetnet.common.packets.Packet;

public class ClientListener {
    public void onReceived(ProtocolType protocolType, Packet packet) {
        System.out.printf("[%s] Received packet %s using %s%n",
                Utils.getCallerClassName(), packet.getShortString(), protocolType.name());
    }

    public void onSent(ProtocolType protocolType, Packet packet) {
        System.out.printf("[%s] Sent packet %s using %s%n",
                Utils.getCallerClassName(), packet.getShortString(), protocolType.name());
    }

    public void onDisconnected() {
        System.out.printf("[%s] Disconnected.%n",
                Utils.getCallerClassName());
    }

    public void onConnected() {
        System.out.printf("[%s] Connected%n",
                Utils.getCallerClassName());
    }
}