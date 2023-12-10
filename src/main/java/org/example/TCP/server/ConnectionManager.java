package org.example.TCP.server;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Callable;

public class ConnectionManager implements Runnable {
    private final Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean closed;

    public ConnectionManager(Socket client) {
        this.clientSocket = client;
        closed = false;
    }

    public boolean isClosed() {
        return closed;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() {
        try {
            if (!clientSocket.isOutputShutdown()) out.close();
            if (!clientSocket.isInputShutdown()) in.close();
            if (!clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.out.println("unexpected error when closing IO sockets.");
            System.exit(1);
        }
        closed = true;
    }

    private File getFileFromResources(String fileName) {
        URL fileURL = EOServer.class.getClassLoader().getResource(fileName);
        if (fileURL == null) {
            System.err.println("Unexpected error: " + fileName + " not found in resources!");
            System.exit(1);
        }

        return closeOnException(
                () -> new File(fileURL.toURI()),
                "Unexpected error: database cannot be converted to URI."
        );
    }

    private String getName(String emailAddress) throws IOException {
        FileReader fileReader = new FileReader(getFileFromResources("database.txt"));
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String currentLine;
        String returnVal = null;

        while((currentLine = bufferedReader.readLine()) != null) {
            String curAddress = currentLine.substring(0, currentLine.indexOf('=')).toLowerCase();
            if (curAddress.equals(emailAddress.toLowerCase())) {
                returnVal = currentLine.substring(currentLine.indexOf('=') + 1);
                break;
            }
        }

        fileReader.close();
        bufferedReader.close();

        return returnVal;
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
        System.out.printf("Waiting for input from %s...\n", clientSocket.getRemoteSocketAddress());

        out = closeOnException(
                () -> new PrintWriter(clientSocket.getOutputStream(), true),
                "Unexpected error during print writer initialization."
        );
        in = closeOnException(
                () -> new BufferedReader(new InputStreamReader(clientSocket.getInputStream())),
                "Unexpected error during buffered reader initialization."
        );

        String inputLine = closeOnException(in::readLine, "Unexpected error when reading buffered reader");
        while (inputLine != null) {
            System.out.printf("Message from %s: \"%s\".\n", clientSocket.getRemoteSocketAddress(), inputLine);
            String finalInputLine = inputLine;
            String name = closeOnException(() -> getName(finalInputLine), "Error while reading name.");

            String response = (name == null) ?
                    String.format("Email address %s is not in the database.", finalInputLine) :
                    name;

            sendMessage(response);
            System.out.printf("Response: \"%s.\"\n", response);

            inputLine = closeOnException(in::readLine, "Unexpected error when reading buffered reader");
        }

        close();
    }
}