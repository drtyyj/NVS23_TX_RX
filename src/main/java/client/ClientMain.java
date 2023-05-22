package client;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

public class ClientMain {
    private DatagramSocket socket;
    private final InetAddress address;

    byte[] buf;

    public ClientMain() throws UnknownHostException {
        address = InetAddress.getByName("localhost");
        buf = new byte[64994];
    }

    public String processSendRequest(String[] input) {
        int port = 4445;
        int dataPacketSize = 1000;
        int sleep = 5;
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
        return sendData(fileName, port, dataPacketSize, sleep);
    }

    public String sendData(String filename, int port, int dataPacketSize, int sleep) {
        try {
            socket = new DatagramSocket();
            TransmissionManager manager = new TransmissionManager("../input/" + filename, dataPacketSize);
            buf = manager.fillBuffer();
            do{
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);
                buf = manager.fillBuffer();
                Thread.sleep(0, sleep);
            } while(buf.length > 0);
            buf = new byte[64994];
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
}