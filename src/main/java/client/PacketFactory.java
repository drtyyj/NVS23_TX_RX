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

public class PacketFactory {
    private final short transmissionId;
    private int currentSequenceNumber;
    private int maxSequenceNumber;
    String fileName;
    private final String filePath;
    private byte[] md5;

    private InputStream dataStream;
    private final int dataPacketSize;


    private PacketFactory(short transmissionId, String filePath, int dataPacketSize) {
        String[] components = filePath.split("/");
        this.transmissionId = transmissionId;
        currentSequenceNumber = 0;
        this.filePath = filePath;
        this.dataPacketSize = dataPacketSize;
        this.fileName = components[components.length -1];
    }

    public static PacketFactory getInstance(short transmissionId, String filePath, int dataPacketSize)
            throws IOException, NoSuchAlgorithmException {
        PacketFactory newFactory = new PacketFactory(
                transmissionId,
                filePath,
                dataPacketSize
        );
        newFactory.setStreamMaxSequenceAndMd5();
        return newFactory;
    }

    public byte[] getPacket() throws IOException{
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

    public void setStreamMaxSequenceAndMd5() throws IOException, NoSuchAlgorithmException {
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

    public int getExpectedAckSequenceNr() {
        return currentSequenceNumber - 1;
    }
}
