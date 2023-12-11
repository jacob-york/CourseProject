package org.example.TCP.server;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EOServer implements Runnable {

    private int port;
    private List<ConnectionManager> connections;
    private ServerSocket serverSocket;
    private ExecutorService pool;

    public EOServer() {
        port = 81;
        connections = new ArrayList<>();
    }

    public void printToConsole(String msg) {
        System.out.println("server> " + msg);
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
        serverSocket = exitOnException(() -> new ServerSocket(port),
                "Could not listen on port: " + port + ".");

        pool = Executors.newCachedThreadPool();

        while (true) {
            printToConsole("Waiting for new connections (active Connections: " + connections.size() + ")...");
            Socket clientSocket = exitOnException(serverSocket::accept, "Accept failed.");
            printToConsole("New ip " + clientSocket.getRemoteSocketAddress() + " connected.");
            ConnectionManager cManager = new ConnectionManager(clientSocket);
            connections.add(cManager);
            pool.execute(cManager);

            int i = 0;
            while (i < connections.size()) {
                ConnectionManager curManager = connections.get(i);
                if (curManager.isClosed()) {
                    connections.remove(curManager);
                }
                else {
                    i++;
                }
            }
        }
    }

    public static void main(String[] args) {
        EOServer server = new EOServer();
        server.run();
    }
}