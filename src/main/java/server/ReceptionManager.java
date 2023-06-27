package server;

import model.PacketProcessing;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReceptionManager {


    private final PacketProcessing processing = new PacketProcessing();

    private final Map<Short, Transmission> activeTransmissions;
    private int windowSize;

    protected ReceptionManager(int windowSize) {
        activeTransmissions = new HashMap<>();
        this.windowSize = windowSize;
    }

    protected int processReceivedData(byte[] data) throws IOException, NoSuchAlgorithmException {
        short transmissionId = processing.getBytesAsShort(data, 0, 2);
        int fileSequenceNumber = 0;
        if(data.length > 65000) {
            activeTransmissions.remove(transmissionId);
            throw new IOException("Packet size exceeds maximum size of 65000 Bytes");
        }
        int sequenceNumber = processing.getBytesAsInt(data, 2, 4);
        Transmission transmission = null;
        if(sequenceNumber == 0) {
             int maxSequenceNr = processing.getBytesAsInt(data, 6, 4);
             String fileName = processing.getBytesAsString(data, 10, data.length - 10);
             transmission = new Transmission(maxSequenceNr, fileName, windowSize);
             activeTransmissions.put(transmissionId, transmission);
        } else if(activeTransmissions.containsKey(transmissionId)) {
            transmission = activeTransmissions.get(transmissionId);
            if(sequenceNumber == transmission.getMaxSeqNumber()) {
                transmission.setMd5(Arrays.copyOfRange(data, 6, data.length));
            }
        }
        if(transmission != null) {
            fileSequenceNumber = transmission.putData(sequenceNumber, Arrays.copyOfRange(data,6, data.length));
            if(transmission.isComplete()) {
                transmission.makeFile();
                deleteTransmission(transmissionId);
            }
        }
        return fileSequenceNumber;
    }

    protected void deleteTransmission(short transmissionId) {
        activeTransmissions.remove(transmissionId);
    }

    protected void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }
}
