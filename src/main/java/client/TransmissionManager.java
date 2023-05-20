package client;

import model.PacketProcessing;

import java.io.IOException;

public class TransmissionManager {

    PacketProcessing processing;

    PacketFactory factory;

    public TransmissionManager(String filename, int dataPacketSize) {
        try {
            factory = PacketFactory.getInstance(
                    (short) (Math.random() * (Math.pow(2, 16) - 1)),
                    filename,
                    dataPacketSize);
            processing = new PacketProcessing();
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
        short transmissionId = processing.getBytesAsShort(ack, 0, 2);
        int sequenceNr = processing.getBytesAsInt(ack, 2, 4);

        if(transmissionId != factory.getTransmissionId() || sequenceNr != factory.getExpectedAckSequenceNr()) {
            throw new RuntimeException("Invalid acknowledgement");
        }
    }
}
