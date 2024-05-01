
package parcial.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class servidor {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 10;
    private ServerSocket serverSocket;
    private ExecutorService pool;

    public servidor() throws IOException {
        serverSocket = new ServerSocket(PORT);
        pool = Executors.newFixedThreadPool(MAX_CLIENTS);
    }

    public void startServer() {
        System.out.println("Server started on port " + PORT);
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute((Runnable) new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
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
        try {
            new servidor().startServer();
        } catch (IOException e) {
            System.out.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


