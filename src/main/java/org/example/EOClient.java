package org.example;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EOClient {

    public static void main(String[] args) throws IOException {
        String serverHostname = args.length > 0 ? args[0] : "127.0.0.1";
        int port = 10007;
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        System.out.println("Attempting to connect to host " + serverHostname + " on port " + port + ".");
        try {
            echoSocket = new Socket(serverHostname, port);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        }
        catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverHostname);
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverHostname);
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        String userInput;

        System.out.print("enter an email address (or \":q\" to quit)>");
        while (!(userInput = scanner.nextLine()).equals(":q")) {
            out.println(userInput.toLowerCase());
            System.out.println("echo: " + in.readLine());
            System.out.print("enter an email address (or \":q\" to quit)>");
        }

        out.close();
        in.close();
        scanner.close();
        echoSocket.close();
    }

}
