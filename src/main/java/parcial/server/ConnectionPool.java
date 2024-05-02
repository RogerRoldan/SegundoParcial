/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parcial.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConnectionPool {
    private static ConnectionPool instance = null;
    private final int maxConnections;
    private final ExecutorService executorService;
    private final LinkedBlockingQueue<ClientHandler> pool;
     private List<ClientHandler> activeClients = new CopyOnWriteArrayList<>();

    // Private constructor for Singleton pattern
    private ConnectionPool(int maxConnections) {
        this.maxConnections = maxConnections;
        this.executorService = Executors.newFixedThreadPool(maxConnections);
        this.pool = new LinkedBlockingQueue<>(maxConnections);

        // Initialize the pool with ClientHandler instances
        for (int i = 0; i < maxConnections; i++) {
            pool.offer(new ClientHandler());
        }
    }

    // Singleton access method
    public static ConnectionPool getInstance(int maxConnections) {
        if (instance == null) {
            instance = new ConnectionPool(maxConnections);
        }
        return instance;
    }

    // Method to get a ClientHandler from the pool
    public ClientHandler acquireConnection() {
        ClientHandler handler = pool.poll();
        if (handler != null) {
            activeClients.add(handler);
        }
        return handler;
    }

    // Method to release a ClientHandler back to the pool
    public void releaseConnection(ClientHandler clientHandler) {
        activeClients.remove(clientHandler);
        pool.offer(clientHandler);
    }

     // Método para obtener los detalles de los clientes activos
    public List<String> getActiveClientDetails() {
        return activeClients.stream()
                            .map(ClientHandler::getClientDetails)
                            .collect(Collectors.toList());
    }

    // Get ExecutorService
    public ExecutorService getExecutorService() {
        return executorService;
    }

    // Shutdown the pool and ExecutorService
    public void shutdownPool() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Executor service shutdown interrupted.");
        } finally {
            executorService.shutdownNow();
        }
    }
}