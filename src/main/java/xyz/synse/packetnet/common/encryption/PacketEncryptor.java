package xyz.synse.packetnet.common.encryption;

import xyz.synse.packetnet.common.packets.Packet;

public class PacketEncryptor {
    public static Packet encrypt(Packet packet, String key) throws Exception {
        return new Packet(packet.getID(), AES256Encryption.encrypt(packet.getData(), key));
    }

    public static Packet decrypt(Packet packet, String key) throws Exception {
        return new Packet(packet.getID(), AES256Encryption.decrypt(packet.getData(), key));
    }
}
