package org.example.TCP.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class EOClient implements Runnable {

    private int port;
    private String hostIP;
    private Socket echoSocket;
    private PrintWriter out;
    private BufferedReader in;

    public EOClient() {
        port = 81;
    }

    public static void main(String[] args) {
        EOClient client = new EOClient();
        client.run();
    }

    public void close() {
        try {
            if (echoSocket != null) {
                if (!echoSocket.isOutputShutdown()) in.close();
                if (!echoSocket.isInputShutdown()) in.close();
                if (!echoSocket.isClosed()) echoSocket.close();
            }
        } catch (IOException e) {
            System.out.println("unexpected error when closing IO sockets.");
            System.exit(1);
        }
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

    public void sendMessage(String message) {
        out.println(message);
    }

    public boolean attemptNewConnection(String candidateIP) {
        System.out.println("Attempting to connect to host " + candidateIP + " on port " + port + "...");
        try {
            echoSocket = new Socket(candidateIP, port);
        } catch (IOException e) {
            System.out.println("Unknown host: " + candidateIP);
            return false;
        }
        try {
            out = new PrintWriter(echoSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Couldn't get output for the connection to: " + candidateIP);
            return false;
        }
        try {
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Couldn't get input for the connection to: " + candidateIP);
            return false;
        }
        System.out.println("Connection successful.");
        return true;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        boolean success = false;
        while (!success) {
            System.out.print("Please enter the Host ip (or quit with ':q')>");
            String candidateIP = scanner.nextLine();
            if (candidateIP.equals(":q")) {
                System.exit(0);
            }
            success = attemptNewConnection(candidateIP);
        }

        System.out.print("enter an email address (disconnect with ':q')>");
        String userInput;
        while (!(userInput = scanner.nextLine()).equals(":q")) {
            sendMessage(userInput.toLowerCase());
            System.out.println("echo> " + exitOnException(in::readLine, "Unexpected input error."));
            System.out.print("enter an email address (disconnect with ':q')>");
        }

        scanner.close();
        close();
    }
}
