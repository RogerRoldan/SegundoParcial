/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parcial.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionPool {
    private static ConnectionPool instance;
    private BlockingQueue<ClientHandler> availableConnections;
    private ExecutorService executorService;

    // Configuración del pool
    private static final int MAX_CONNECTIONS = 10; // Puedes ajustar este número según las necesidades de tu aplicación

    private ConnectionPool() {
        // Crear una cola para manejar las conexiones disponibles
        availableConnections = new LinkedBlockingQueue<>(MAX_CONNECTIONS);
        executorService = Executors.newFixedThreadPool(MAX_CONNECTIONS);

        // Pre-crear objetos ClientHandler y ponerlos en la cola
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            availableConnections.offer(new ClientHandler(null)); // Aquí null es temporal, debería asignarse un socket en el momento de uso
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    public ClientHandler acquireConnection() throws InterruptedException {
        // Tomar una conexión del pool, bloqueará si no hay conexiones disponibles
        return availableConnections.take();
    }

    public void releaseConnection(ClientHandler clientHandler) {
        // Devuelve la conexión al pool y está lista para ser usada de nuevo
        if (clientHandler != null) {
            availableConnections.offer(clientHandler);
        }
    }

    public void shutdownPool() {
        executorService.shutdown();
    }
}