package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ServerMain implements Runnable {
    private final int port;
    private final DatagramSocket socket;
    private boolean running;
    private final ReceptionManager manager;

    private Boolean awaitAck = true;

    private int windowSize = 10;

    byte[] buf;

    public ServerMain(int port) throws SocketException {
        this.port = port;
        running = true;
        socket = new DatagramSocket(port);
        manager = new ReceptionManager(windowSize);
    }

    public void run() {
        System.out.println("Server listening to port " + port);
        int fileSeqNumber;
        while (running) {
            try {
                buf = new byte[65000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                fileSeqNumber = manager.processReceivedData(Arrays.copyOf(packet.getData(), packet.getLength()));
                if(awaitAck) {
                    if(windowSize > 0) {
                        if(fileSeqNumber > 0) {
                            byte[] cuAck = ByteBuffer.allocate(6)
                                    .put(packet.getData(), 0, 2)
                                    .putInt(fileSeqNumber).array();

                            socket.send(new DatagramPacket(cuAck, 6, packet.getAddress(), packet.getPort()));
                        }
                    } else {
                        socket.send(new DatagramPacket(packet.getData(), 6, packet.getAddress(), packet.getPort()));
                    }
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
