package org.example.TCP.server;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EOServer implements Runnable {

    private int port;
    private List<ConnectionManager> connections;
    private ServerSocket serverSocket;
    private boolean closed;
    private ExecutorService pool;

    public EOServer() {
        port = 10007;
        connections = new ArrayList<>();
        closed = false;
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

    private boolean isClosed() {
        return closed;
    }

    @Override
    public void run() {
        serverSocket = closeOnException(() -> new ServerSocket(port),
                "Could not listen on port: " + port + ".");

        pool = Executors.newCachedThreadPool();

        Scanner scanner = new Scanner(System.in);
        while(!isClosed()) {
            // establishing a connection because this is TCP
            System.out.printf("Waiting for connection (active Connections: %d)...\n", connections.size());
            Socket clientSocket = closeOnException(serverSocket::accept, "Accept failed.");
            System.out.printf("Connection from client on ip %s.\n", clientSocket.getRemoteSocketAddress());
            ConnectionManager cManager = new ConnectionManager(clientSocket);
            connections.add(cManager);
            pool.execute(cManager);

            int i = 0;
            while (i < connections.size()) {
                ConnectionManager curManager = connections.get(i);
                if (curManager.isClosed()) {
                    connections.remove(curManager);
                } else {
                    i++;
                }
            }
        }
        scanner.close();
    }

    public void close() {
        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("unexpected error when closing server socket.");
                close();
            }
        }
        for (ConnectionManager connection : connections) {
            connection.close();
        }
        closed = true;
    }

    public static void main(String[] args) {
        EOServer server = new EOServer();
        server.run();
    }
}