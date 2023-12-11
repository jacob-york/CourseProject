package org.example.UDP.client;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class EOClient implements Runnable {

    private int port;
    private InetAddress hostIP;
    private byte[] buffer;
    public EOClient() {
        port = 81;
    }

    public static void main(String[] args) {
        EOClient client = new EOClient();
        client.run();
    }

    private <R> R exitOnException(Callable<R> callable, String errorMsg) {
        R returnVal = null;
        try {
            returnVal = callable.call();
        } catch (Exception e) {
            System.out.println(errorMsg);
            System.exit(1);
        }
        return returnVal;
    }

    @Override
    public void run() {
        DatagramSocket datagramSocket = exitOnException(DatagramSocket::new, "Socket could not be bound.");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the Host ip>");
        hostIP = exitOnException(InetAddress::getLocalHost, "Invalid host.");

        System.out.print("enter an email address (or \":q\" to quit)>");
        String userInput;
        while (!(userInput = scanner.nextLine()).equals(":q")) {
            buffer = userInput.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, hostIP, port);
            try {
                datagramSocket.send(datagramPacket);
                DatagramPacket receivingPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(receivingPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.print("enter an email address (or \":q\" to quit)>");
        }

        datagramSocket.close();
    }
}
