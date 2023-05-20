package client;

import java.io.IOException;

public class TransmissionManager {

    PacketFactory factory;

    public TransmissionManager(String filename, int dataPacketSize) {
        try {
            factory = PacketFactory.getInstance(
                    (short) (Math.random() * (Math.pow(2, 16) - 1)),
                    filename,
                    dataPacketSize);
        }catch (Exception e) {
            factory = null;
            throw new RuntimeException(e);
        }
    }

    public int fillBuffer(byte[] buf) throws IOException {
        try{
            byte[] packet = factory.getPacket();
            System.arraycopy(packet, 0, buf, 0, packet.length);
            return packet.length;
        } catch(NullPointerException e) {
            throw new RuntimeException("Cannot create Packets, no active Transmission.");
        }
    }

    public void processAck(byte[] ack) {

    }
}
