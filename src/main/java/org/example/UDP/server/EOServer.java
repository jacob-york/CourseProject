package org.example.UDP.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.Callable;

public class EOServer implements Runnable {

    private int port;
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];

    public EOServer() {
        port = 81;
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

    private File getFileFromResources(String fileName) {
        URL fileURL = org.example.TCP.server.EOServer.class.getClassLoader().getResource(fileName);
        if (fileURL == null) {
            System.out.println("Unexpected error: " + fileName + " not found in resources!");
            System.exit(1);
        }

        return exitOnException(() -> new File(fileURL.toURI()),
                "Unexpected error: database cannot be converted to URI.");
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

    @Override
    public void run() {
        while (true) {
            try {
                datagramSocket = exitOnException(DatagramSocket::new, "Error initializing datagramSocket.");
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                InetAddress senderAddr = datagramPacket.getAddress();
                int port = datagramPacket.getPort();
                String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                System.out.println(message);
                datagramSocket.close();

                byte[] sendBuffer = getName(message).getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, senderAddr, port);
                datagramSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) {
        EOServer server = new EOServer();
        server.run();
    }
}