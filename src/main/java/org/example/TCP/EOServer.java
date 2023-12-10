package org.example.TCP;

import java.net.*;
import java.io.*;

public class EOServer implements Runnable {

    public static int port = 10007;
    public static int capacity = 4;

    public static File getFileFromResources(String fileName) {
        URL url = EOServer.class.getClassLoader().getResource(fileName);

        if (url == null) {
            System.err.println("fatal error: " + fileName + " not found in resources!");
            System.exit(1);
        }
        File file = null;

        try {
            file = new File(url.toURI());
        }
        catch (URISyntaxException e) {
            System.err.println("Unexpected error: database cannot be converted to URI.");
            e.printStackTrace();
            System.exit(1);
        }

        return file;
    }

    public static String getName(String emailAddress) throws IOException {
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

        return returnVal == null ? "Email address: " + emailAddress + " is not in the database." : returnVal;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e) {
            System.err.println("Could not listen on port: " + port + ".");
            System.exit(1);
        }

        Socket clientSocket = null;
        System.out.println("Waiting for connection.....");
        try {
            clientSocket = serverSocket.accept();
        }
        catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        System.out.println("Connection success");
        System.out.println("Waiting for input...");

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            System.out.println("Server: " + inputLine);
            String name = getName(inputLine);
            out.println(name);
        }

        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
    }

    @Override
    public void run() {

    }
}