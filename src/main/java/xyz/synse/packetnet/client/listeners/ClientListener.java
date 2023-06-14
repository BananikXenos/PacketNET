package xyz.synse.packetnet.client.listeners;

import xyz.synse.packetnet.common.KDebug;
import xyz.synse.packetnet.common.ProtocolType;
import xyz.synse.packetnet.common.packets.Packet;

public class ClientListener {
    public void onReceived(ProtocolType protocolType, Packet packet) {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Received packet " + packet.getShortString() + " using " + protocolType.name());
    }

    public void onSent(ProtocolType protocolType, Packet packet) {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Sent packet " + packet.getShortString() + " using " + protocolType.name());
    }

    public void onDisconnected() {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Disconnected.");
    }

    public void onConnected() {
        System.out.println("[" + KDebug.getCallerClassShortName() + "] Connected");
    }
}
