
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
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = connectionPool.acquireConnection();
                if (clientHandler == null) {
                    DataOutputStream tempOutput = new DataOutputStream(clientSocket.getOutputStream());
                    tempOutput.writeUTF("Connection refused: maximum connections reached.");
                    tempOutput.close();
                    clientSocket.close();
                } else {
                    clientHandler.setSocket(clientSocket);

                        // Obtener el servicio de ejecución del pool de conexiones
                        ExecutorService executor = connectionPool.getExecutorService();

                        // Definir la tarea a ejecutar en el servicio de ejecución
                        Runnable task = () -> {
                            // Ejecutar la lógica del controlador de cliente
                            clientHandler.run();
                            
                            // Devolver la conexión al pool después de desconectar
                            connectionPool.releaseConnection(clientHandler);
                        };

                        // Ejecutar la tarea en el servicio de ejecución
                        executor.execute(task);
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
   
