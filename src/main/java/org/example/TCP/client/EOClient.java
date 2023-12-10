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

    public EOClient(String hostIP) {
        port = 10007;
        this.hostIP = hostIP;
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
    }

    public static void main(String[] args) {
        EOClient client = new EOClient(args.length > 0 ? args[0] : "192.168.50.90");
        client.run();
    }

    public void close() {
        try {
            if (!echoSocket.isOutputShutdown()) in.close();
            if (!echoSocket.isInputShutdown()) in.close();
            if (!echoSocket.isClosed()) echoSocket.close();
        } catch (IOException e) {
            System.out.println("unexpected error when closing IO sockets.");
            System.exit(1);
        }
    }

    private <R> R closeOnException(Callable<R> callable, String errorMsg) {
        R returnVal = null;
        try {
            returnVal = callable.call();
        } catch (Exception e) {
            System.out.println(errorMsg);
            close();
        }
        return returnVal;
    }

    @Override
    public void run() {
        System.out.println("Attempting to connect to host " + hostIP + " on port " + port + "...");

        echoSocket = closeOnException(() -> new Socket(hostIP, port), "Unknown host: " + hostIP);
        out = closeOnException(
                () -> new PrintWriter(echoSocket.getOutputStream(), true),
                "Couldn't get output for the connection to: " + hostIP
        );
        in = closeOnException(
                () -> new BufferedReader(new InputStreamReader(echoSocket.getInputStream())),
                "Couldn't get input for the connection to: " + hostIP
        );

        Scanner scanner = new Scanner(System.in);
        String userInput;

        System.out.print("enter an email address (or \":q\" to quit)>");
        while (!(userInput = scanner.nextLine()).equals(":q")) {
            out.println(userInput.toLowerCase());
            System.out.println("echo: " + closeOnException(in::readLine, "Unexpected input error."));
            System.out.print("enter an email address (or \":q\" to quit)>");
        }

        scanner.close();
        close();
    }
}
