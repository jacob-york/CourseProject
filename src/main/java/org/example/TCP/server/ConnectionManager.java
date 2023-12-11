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
    public boolean userQuit;

    public ConnectionManager(Socket client) {
        this.clientSocket = client;
        closed = false;
        userQuit = false;
    }

    public boolean isClosed() {
        return closed;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void printToConsole(String msg) {
        System.out.println("connection-manager> " + msg);
    }

    public void close() {
        try {
            if (!clientSocket.isOutputShutdown()) out.close();
            if (!clientSocket.isInputShutdown()) in.close();
            if (!clientSocket.isClosed()) clientSocket.close();
            printToConsole(clientSocket.getRemoteSocketAddress() + " disconnected.");

        } catch (IOException e) {
            printToConsole("Unexpected error when closing IO sockets.");
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

        return exitOnException(
                () -> new File(fileURL.toURI()),
                "Unexpected error: database cannot be converted to URI."
        );
    }


    private String getName(String emailAddress) throws IOException {
        File file = getFileFromResources("database.txt");
        FileReader fileReader = new FileReader(file);
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

    private <R> R exitOnException(Callable<R> callable, String errorMsg) {
        R returnVal = null;
        try {
            returnVal = callable.call();
        } catch (Exception e) {
            printToConsole(errorMsg);
            System.exit(1);
        }
        return returnVal;
    }

    @Override
    public void run() {
        printToConsole(String.format("Waiting for input from " + clientSocket.getRemoteSocketAddress() + "..."));

        out = exitOnException(() -> new PrintWriter(clientSocket.getOutputStream(), true),
                "Unexpected error during print writer initialization.");
        in = exitOnException(() -> new BufferedReader(new InputStreamReader(clientSocket.getInputStream())),
                "Unexpected error during buffered reader initialization.");
        String inputLine = exitOnException(in::readLine, "Unexpected error when reading buffered reader.");

        while (inputLine != null) {
            printToConsole("Message from " + clientSocket.getRemoteSocketAddress() +": \"" + inputLine + "\".");
            String finalInputLine = inputLine;
            String name = exitOnException(() -> getName(finalInputLine), "Error while reading name.");

            String response = (name == null) ?
                    "Email address \"" + finalInputLine + "\" is not in the database." : name;

            sendMessage(response);
            printToConsole("Response: \"" + response + "\".");
            inputLine = exitOnException(in::readLine, "Unexpected error when reading buffered reader");
        }

        close();
    }
}