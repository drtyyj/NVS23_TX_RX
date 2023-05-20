package client;

import java.io.File;
import java.net.*;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class ClientMain {
    private DatagramSocket socket;
    private final InetAddress address;

    byte[] buf;

    public ClientMain() throws UnknownHostException {
        address = InetAddress.getByName("localhost");
        buf = new byte[1028];
    }

    public String processSendRequest(String[] input) {
        int port = 4445;
        int dataPacketSize = 260;
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
                    default:
                        return "Invalid Input";
                }
            }
        } catch(IndexOutOfBoundsException e) {
            return "Invalid Input";
        } catch(Exception e) {
            return e.getMessage();
        }
        return sendData(fileName, port, dataPacketSize);
    }

    public String sendData(String filename, int targetPort, int dataPacketSize) {
        try {
            socket = new DatagramSocket(4440);
            socket.setSoTimeout(1000);
            int transmissionAttempts = 0;

            TransmissionManager manager = new TransmissionManager("../input/" + filename, dataPacketSize);
            DatagramPacket ackPacket = new DatagramPacket(buf, 0, 6);
            int length = manager.fillBuffer(buf);
            do{
                if(transmissionAttempts == 0) {
                    DatagramPacket dataPacket = new DatagramPacket(buf, length, address, targetPort);
                    socket.send(dataPacket);
                }

                try {
                    socket.receive(ackPacket);
                    manager.processAck(Arrays.copyOf(ackPacket.getData(), ackPacket.getLength()));
                    transmissionAttempts = 0;
                } catch(SocketTimeoutException e) {
                    transmissionAttempts++;
                    if(transmissionAttempts >= 5)
                        throw new RuntimeException("Maximum amount of transmission attempts for packet reached, aborting transmission");
                    continue;
                }
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
        if(returnSize > 1028)
            throw new InvalidParameterException("Data Packet Size mustn't exceed" +  (buf.length - 6) + "Bytes");
        return returnSize;
    }
}