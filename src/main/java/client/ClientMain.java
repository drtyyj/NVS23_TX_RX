package client;

import exceptions.TransmissionException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class ClientMain {

    private TransmissionManager manager;
    private DatagramSocket socket;
    private final InetAddress address;

    private Boolean awaitAck = true;
    private final byte[] buf;
    private int port;
    private int dataPacketSize;
    private int sleep;
    private int transmissionAttempts;

    public ClientMain() throws UnknownHostException {
        address = InetAddress.getByName("localhost");
        buf = new byte[64994];
    }

    public String processSendRequest(String[] input) {
        port = 4445;
        dataPacketSize = 1000;
        sleep = 5;
        if(input.length == 1)
            return "Invalid Input: Missing name of file to be sent.";
        if(invalidFileName(input[1]))
            return "File " + input[1] + " not found.";
        String fileName = input[1];
        try {
            for (int i = 2; i < input.length; i = i + 2) {
                switch (input[i]) {
                    case "-d":
                        dataPacketSize = processPackageSize(input[i+1]);
                        break;
                    case "-p":
                        port = processPort(input[i+1]);
                        break;
                    case "-s":
                        sleep = processSleep(input[i+1]);
                        break;
                    default:
                        return "Invalid Input";
                }
            }
        } catch(IndexOutOfBoundsException e) {
            return "Invalid Input";
        } catch(Exception e) {
            return e.getMessage();
        }
        return sendData(fileName);
    }

    public String sendData(String filename) {
        try {
            socket = new DatagramSocket(4440);
            socket.setSoTimeout(1000);
            transmissionAttempts = 0;

            manager = new TransmissionManager("../input/" + filename, dataPacketSize);
            int length = manager.fillBuffer(buf);
            do{
                sendPacket(length);
                if(awaitAck) {
                    try {
                        receiveAck();
                    } catch (SocketTimeoutException e) {
                        transmissionAttempts++;
                        if (transmissionAttempts >= 5)
                            throw TransmissionException.maxAttempts();
                        continue;
                    }
                }
                if(transmissionAttempts == 0)
                    length = manager.fillBuffer(buf);
            } while(length > 0);
            Arrays.fill(buf, (byte) 0);
            return "File sent";
        } catch (Exception e) {
                return "Transmission error: " + e.getMessage();
        } finally {
            try {
                socket.close();
            } catch (Exception ignore) {}
        }
    }

    private void sendPacket(int length) throws InterruptedException, IOException {
        Thread.sleep(0, sleep);
        DatagramPacket dataPacket = new DatagramPacket(buf, length, address, port);
        socket.send(dataPacket);
    }

    private void receiveAck() throws IOException {
        DatagramPacket ackPacket = new DatagramPacket(buf, 0, 6);
        socket.receive(ackPacket);
        manager.processAck(Arrays.copyOf(ackPacket.getData(), ackPacket.getLength()));
        transmissionAttempts = 0;
    }

    private boolean invalidFileName(String fileName) {
        return !new File("../input/" + fileName).exists();
    }

    private int processPort(String port) {
        int returnPort = Integer.parseUnsignedInt(port);
        if(returnPort <= 0)
            throw new InvalidParameterException("Invalid Input");
        return returnPort;
    }

    private int processPackageSize(String size) {
        int returnSize = Integer.parseUnsignedInt(size);
        if(returnSize <= 0)
           throw new InvalidParameterException("Invalid Input");
        if(returnSize > 64994)
            throw new InvalidParameterException("Data Packet Size mustn't exceed 64994 Bytes");
        return returnSize;
    }

    private int processSleep(String sleep) {
        int returnSleep = Integer.parseUnsignedInt(sleep);
        if(returnSleep < 0)
            throw new InvalidParameterException("Sleep time between sending packages cannot be negative");
        if(returnSleep > 1000000)
            throw new InvalidParameterException("Sleep time cannot be bigger than 1ms");
        return returnSleep;
    }

    public void setAwaitAck(Boolean awaitAck) {
        this.awaitAck = awaitAck;
    }
}