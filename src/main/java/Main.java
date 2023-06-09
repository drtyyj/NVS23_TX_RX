import client.ClientMain;
import server.ServerMain;

import java.util.Scanner;


public class Main {
    private static boolean running;
    private static ClientMain client;
    private static ServerMain server;
    private static Scanner scanner;

    public static void main(String[] args) {
        init(args);
        while(running) {
            System.out.println("Waiting for Input ('quit' or 'send file.* [-p portNumber] [-d dataPacketSize] [-s sleepTime]'");
            System.out.println("or 'enableack'/'disableack'):");
            String[] input = scanner.nextLine().split(" ");
            switch (input[0]) {
                case "quit":
                    quit();
                    break;
                case "send":
                    System.out.println(client.processSendRequest(input));
                    break;
                case "enableack":
                    client.setAwaitAck(true);
                    server.setAwaitAck(true);
                    break;
                case "disableack":
                    client.setAwaitAck(false);
                    server.setAwaitAck(false);
                    break;
                default:
                    System.out.println("Invalid Input");
            }
        }
        System.out.println("Client closed");
    }

    private static void init(String[] args) {
        int listeningPort;
        if(args.length == 0) {
            listeningPort = 4445;
        }else listeningPort = Integer.parseInt(args[0]);

        try {
            running = true;
            scanner = new Scanner(System.in);
            client = new ClientMain();
            server = new ServerMain(listeningPort);
            Thread thread = new Thread(server);
            thread.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            quit();
        }
    }

    private static void quit() {
        System.out.println("Quitting");
        running = false;
        server.stopRunning();
        scanner.close();
        client = null;
    }
}
