
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
                System.out.println("Waiting for clients...");
                Socket clientSocket = serverSocket.accept();

                ClientHandler clientHandler = connectionPool.acquireConnection();
                if (clientHandler == null) {
                    // Envía un mensaje al cliente si no hay conexiones disponibles
                    DataOutputStream tempOutput = new DataOutputStream(clientSocket.getOutputStream());
                    tempOutput.writeUTF("Connection refused: maximum connections reached.");
                    tempOutput.close();
                    clientSocket.close();
                    System.out.println("Refused client: " + clientSocket.getInetAddress());
                } else {
                    System.out.println("Client connected from " + clientSocket.getInetAddress());
                    clientHandler.setSocket(clientSocket);
                    connectionPool.getExecutorService().execute(clientHandler);
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
   
