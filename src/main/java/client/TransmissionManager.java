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

    public byte[] fillBuffer() throws IOException {
        try{
            return factory.getPacket();
        } catch(NullPointerException e) {
            throw new RuntimeException("Cannot create Packets, no active Transmission.");
        }
    }
}
