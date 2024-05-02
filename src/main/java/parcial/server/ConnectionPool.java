/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parcial.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {
    private static ConnectionPool instance = null;
    private final int maxConnections;
    private final ExecutorService executorService;
    private final LinkedBlockingQueue<ClientHandler> pool;

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
    public ClientHandler acquireConnection() throws InterruptedException {
        return pool.take();
    }

    // Method to release a ClientHandler back to the pool
    public void releaseConnection(ClientHandler clientHandler) {
        if (clientHandler != null) {
            pool.offer(clientHandler);
        }
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