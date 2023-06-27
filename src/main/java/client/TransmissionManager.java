package client;

import model.PacketProcessing;

import java.io.IOException;

public class TransmissionManager {

    PacketProcessing processing;

    PacketFactory factory;

    public TransmissionManager(String filename, int dataPacketSize, int windowSize) {
        try {
            factory = PacketFactory.getInstance(
                    (short) (Math.random() * (Math.pow(2, 16) - 1)),
                    filename,
                    dataPacketSize,
                    windowSize);
            processing = new PacketProcessing();
        }catch (Exception e) {
            factory = null;
            throw new RuntimeException(e);
        }
    }

    public int fillBuffer(byte[] buf) {
        try{
            byte[] packet = factory.getPacketFromWindow();
            System.arraycopy(packet, 0, buf, 0, packet.length);
            return packet.length;
        } catch(NullPointerException e) {
            throw new RuntimeException("Cannot create Packets, no active Transmission.");
        }
    }

    public boolean processAck(byte[] ack) {
        short transmissionId = processing.getBytesAsShort(ack, 0, 2);
        int sequenceNr = processing.getBytesAsInt(ack, 2, 4);
        if(transmissionId != factory.getTransmissionId()) {
            throw new RuntimeException("Invalid acknowledgement");
        }
        return factory.processAck(sequenceNr);
    }

    public void loadNextWindow() {
        factory.loadNextWindow();
    }

    public void resetWindow() {
        factory.resetCurrentWindowIndex();
    }
}
