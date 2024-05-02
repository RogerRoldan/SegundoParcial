
package parcial.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class servidor {
    private int port;
    private int maxClients = 2;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private ConnectionPool connectionPool;

    public servidor(int port) {
        this.port = port;
        this.connectionPool = ConnectionPool.getInstance(maxClients);
        this.isRunning = true;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port: " + port);

            while (isRunning) {
                try {
                    System.out.println("Waiting for clients...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected from " + clientSocket.getInetAddress());

                    // Utiliza el pool para manejar la conexión
                    ClientHandler clientHandler = connectionPool.acquireConnection();
                    clientHandler.setSocket(clientSocket);

                    // Ejecuta el handler en un nuevo hilo
                    connectionPool.getExecutorService().execute(clientHandler);

                } catch (InterruptedException e) {
                    System.out.println("Error acquiring a connection from the pool: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        isRunning = false;
        connectionPool.shutdownPool();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            System.out.println("Server stopped.");
        } catch (IOException e) {
            System.out.println("Error closing server: " + e.getMessage());
        }
    }
    public static void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directorio creado: " + directoryPath);
            } else {
                System.out.println("No se pudo crear el directorio: " + directoryPath);
            }
        }
    }

    public static void main(String[] args) {
         String dirPath = "server_files/";
        createDirectoryIfNotExists(dirPath);
        int port = 12345;  // Asumiendo que el servidor corre en el puerto 8000
        servidor server = new servidor(port);
        server.startServer();
    }
}
   
