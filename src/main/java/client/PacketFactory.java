package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PacketFactory {
    private final short transmissionId;
    private int currentSequenceNumber;
    private int maxSequenceNumber;
    String fileName;
    private final String filePath;
    private byte[] md5;

    private InputStream dataStream;
    private final int dataPacketSize;
    private final byte[][] window;
    private int currentWindowIndex;
    private int currentAck;


    private PacketFactory(short transmissionId, String filePath, int dataPacketSize, int windowSize) {
        String[] components = filePath.split("/");
        this.transmissionId = transmissionId;
        currentSequenceNumber = 0;
        this.filePath = filePath;
        this.dataPacketSize = dataPacketSize;
        this.fileName = components[components.length-1];
        this.window = new byte[windowSize][];
        currentAck = -1;
        currentWindowIndex = 0;
    }

    public static PacketFactory getInstance(short transmissionId, String filePath, int dataPacketSize, int windowSize)
            throws IOException, NoSuchAlgorithmException {
        PacketFactory newFactory = new PacketFactory(
                transmissionId,
                filePath,
                dataPacketSize,
                windowSize
        );
        newFactory.setStreamMaxSequenceAndMd5();
        newFactory.loadNextWindow();
        return newFactory;
    }

    public byte[] getPacketFromWindow() {
        if (currentWindowIndex >= window.length) {
            loadNextWindow();
        }
        if(window[currentWindowIndex] == null)
            return new byte[0];
        return window[currentWindowIndex++];
    }

    public void loadNextWindow() {
        try{
            Arrays.fill(window, null);
            for(int i = 0; i < window.length; i++) {
                window[i] = getPacket();
            }
        } catch (IOException ignore) {}
        currentWindowIndex = 0;
    }

    private void shiftWindow(int amount) {
        int firstNullIndex = 0;
        try {
            for(int i = amount; i < window.length; i++) {
                window[i - amount] = window[i];
            }
            for(int i = window.length - amount; i < window.length; i++) {
                firstNullIndex = i;
                window[i] = getPacket();
            }
        } catch (IOException e) {
            Arrays.fill(window, firstNullIndex, window.length, null);
        }
        currentWindowIndex = 0;
    }

    public void resetCurrentWindowIndex() {
        currentWindowIndex = 0;
    }

    public boolean processAck(int ackSequenceNumber) {
        int shiftamount = ackSequenceNumber - currentAck;
        if(shiftamount >= 0) {
            shiftWindow(ackSequenceNumber - currentAck);
            currentAck = ackSequenceNumber;
        }
        return currentAck == maxSequenceNumber;
    }

    private byte[] getPacket() throws IOException{
        try {
            byte[] bytes;
            if (currentSequenceNumber > maxSequenceNumber) {
                return new byte[0];
            } else if (currentSequenceNumber == 0) {
                bytes = ByteBuffer.allocate(10 + fileName.getBytes().length)
                        .putShort(transmissionId)
                        .putInt(currentSequenceNumber)
                        .putInt(maxSequenceNumber)
                        .put(fileName.getBytes()).array();
            } else if (currentSequenceNumber == maxSequenceNumber) {
                bytes = ByteBuffer.allocate(22)
                        .putShort(transmissionId)
                        .putInt(currentSequenceNumber)
                        .put(md5).array();
                dataStream.close();
            } else {
                if(dataStream.available() < dataPacketSize)
                    bytes = ByteBuffer.allocate(6 + dataStream.available())
                            .putShort(transmissionId)
                            .putInt(currentSequenceNumber)
                            .put(dataStream.readAllBytes()).array();
                else
                    bytes = ByteBuffer.allocate(6 + dataPacketSize)
                        .putShort(transmissionId)
                        .putInt(currentSequenceNumber)
                        .put(dataStream.readNBytes(dataPacketSize)).array();
            }
            currentSequenceNumber++;
            return bytes;
        } catch (IOException e) {
            dataStream.close();
            throw new RuntimeException();
        }
    }

    private void setStreamMaxSequenceAndMd5() throws IOException, NoSuchAlgorithmException {
        dataStream = new FileInputStream(filePath);

        Path path = Paths.get(filePath);
        long bytes = Files.size(path);
        maxSequenceNumber = (int) Math.ceil(bytes / (double) dataPacketSize) + 1;

        byte[] data = Files.readAllBytes(path);
        md5 = MessageDigest.getInstance("MD5").digest(data);
    }

    public short getTransmissionId() {
        return transmissionId;
    }
}
