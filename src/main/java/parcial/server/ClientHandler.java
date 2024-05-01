/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parcial.server;

import java.io.*;
import java.net.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error setting up stream: " + e.getMessage());
            close();
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while (true) {
                // Espera recibir un mensaje del cliente
                line = input.readUTF();
                System.out.println(line);
                if (line != null) {
                    processCommand(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from or writing to client: " + e.getMessage());
        } finally {
            System.out.println("si");
            close();
        }
    }

    private void processCommand(String line) {
        try {
            // Aquí deserializamos el mensaje JSON recibido
            Map<String, Object> command = objectMapper.readValue(line, Map.class);
            String type = (String) command.get("type");
            
            switch (type) {
                case "list_files":
                    listFiles();
                    break;
                case "send_file":
                    sendFile((String) command.get("filename"));
                    break;
                case "receive_file":
                    receiveFile((String) command.get("filename"));
                    break;
                default:
                    output.writeUTF("Unknown command");
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
         System.out.println("3");
    }

    private void listFiles() throws IOException {
        // Listar archivos del servidor
        File folder = new File("server_files/");
        File[] listOfFiles = folder.listFiles();
        List<Map<String, Object>> files = new ArrayList<>();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("name", file.getName());
                fileInfo.put("size", file.length());
                fileInfo.put("extension", getFileExtension(file));
                files.add(fileInfo);
            }
        }

        String jsonResponse = objectMapper.writeValueAsString(files);
        output.writeUTF(jsonResponse);
    }

    private void sendFile(String filename) throws IOException {
        File file = new File("server_files/" + filename);
        if (file.exists()) {
            output.writeUTF("File found");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            fis.close();
        } else {
            output.writeUTF("File not found");
        }
    }

    private void receiveFile(String filename) throws IOException {
        File file = new File("server_files/" + filename);
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            System.out.println("1");
            fos.write(buffer, 0, read);
            System.out.println("2");
        }
        System.out.println("3");
        fos.close();
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }

    private void close() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing streams or socket: " + e.getMessage());
        }
    }
}
