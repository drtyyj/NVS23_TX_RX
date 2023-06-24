package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class ServerMain implements Runnable {
    private final int port;
    private final DatagramSocket socket;
    private boolean running;
    private final ReceptionManager manager;

    private Boolean awaitAck = true;

    byte[] buf;

    public ServerMain(int port) throws SocketException {
        this.port = port;
        running = true;
        socket = new DatagramSocket(port);
        manager = new ReceptionManager();
    }

    public void run() {
        System.out.println("Server listening to port " + port);
        while (running) {
            try {
                buf = new byte[65000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                manager.processReceivedData(Arrays.copyOf(packet.getData(), packet.getLength()));
                if(awaitAck) {
                    socket.send(new DatagramPacket(packet.getData(), 6, packet.getAddress(), packet.getPort()));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void stopRunning() {
        running = false;
        socket.close();
        System.out.println("Server closed");
    }

    public void setAwaitAck(Boolean awaitAck) {
        this.awaitAck = awaitAck;
    }
}
