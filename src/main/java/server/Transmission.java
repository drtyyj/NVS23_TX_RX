package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Transmission {

    private final int maxSeqNumber;
    private int fileSeqNumber;
    private final String fileName;
    private final List<byte[]> fileData;

    // TreeMap is sorted by key
    private TreeMap<Integer, byte[]> windowBuffer;
    private final int windowSize;
    private byte[] md5;

    public Transmission(int maxSeqNumber, String fileName, int windowSize) {
        fileData = new ArrayList<>();
        this.maxSeqNumber = maxSeqNumber;
        fileSeqNumber = 0;
        this.fileName = fileName;
        this.windowSize = windowSize;
        windowBuffer = new TreeMap<>();
    }

    public int getMaxSeqNumber() {
        return maxSeqNumber;
    }

    public int putData(int sequenceNumber, byte[] data) {

        windowBuffer.put(sequenceNumber, data);

        if(windowBuffer.size() == windowSize || sequenceNumber == maxSeqNumber) {
            for(Map.Entry<Integer, byte[]> fragment : windowBuffer.entrySet()) {
                if (fragment.getKey() - fileSeqNumber <= 1) {
                    fileData.add(fragment.getKey(), fragment.getValue());
                    fileSeqNumber = fragment.getKey();
                    //windowBuffer.remove(fragment.getKey());
                } else {
                    break;
                }
            }
            windowBuffer.clear();
            return fileSeqNumber;
        }
        return 0;
    }

    public void setMd5(byte[] md5) {
        this.md5 = md5;
    }

    public Boolean isComplete() {
        return fileData.size() == maxSeqNumber + 1;
    }

    public void makeFile() throws IOException, NoSuchAlgorithmException {
        byte[] newData = new byte[0];
        for(int i = 1; i < fileData.size() - 1; i++) {
            byte[] data = fileData.get(i);
            newData = Arrays.copyOf(newData, newData.length + data.length);
            System.arraycopy(data, 0, newData, newData.length - data.length, data.length);
        }
        byte[] calcMd5 = MessageDigest.getInstance("MD5").digest(newData);

        if (java.util.Arrays.equals(md5, calcMd5)) {
            FileOutputStream outputStream = new FileOutputStream("../output/" + fileName);
            outputStream.write(newData);
            System.out.println("File " + fileName + " received");
        }
    }

    public int findEOF() {
        byte[] lastData = fileData.get(fileData.size() - 2);
        int i;
        for(i = 1; i < lastData.length; i++) {
            if(lastData[i] == (byte) 0)
                return i;
        }
        return 0;
    }
}
