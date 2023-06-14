package xyz.synse.packetnet.server;

import java.net.Socket;
import java.util.Optional;

public class Connection {
    private final Socket tcpSocket;
    private Optional<Integer> udpPort;

    public Connection(Socket tcpSocket) {
        this.tcpSocket = tcpSocket;
        this.udpPort = Optional.empty();
    }

    public Connection(Socket tcpSocket, int udpPort) {
        this.tcpSocket = tcpSocket;
        this.udpPort = Optional.of(udpPort);
    }

    public Socket getTcpSocket() {
        return tcpSocket;
    }

    public Optional<Integer> getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = Optional.of(udpPort);
    }

    public void removeUdpPort(){
        this.udpPort = Optional.empty();
    }
}
