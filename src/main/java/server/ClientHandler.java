/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parcial.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.*;
import java.net.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler() {
    }

    public void setSocket(Socket socket) {
        this.clientSocket = socket;
        initializeDataStreams();
        try {
            output.writeUTF("Welcome, connection accepted.");
        } catch (IOException e) {
            System.err.println("Error sending welcome message: " + e.getMessage());
        }
    }
    
    private void initializeDataStreams() {
        try {
            // Cierra flujos antiguos si existen
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            
            // Establece los nuevos flujos de entrada y salida para el socket
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error initializing data streams: " + e.getMessage());
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
            close();
            System.out.println("cerro coneccion");
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
                    receiveFile((String) command.get("filename"), (String) command.get("filesize"));
                    break;
                default:
                    output.writeUTF("Unknown command");
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
        
    }
    public void processMessage(String jsonMessage) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(jsonMessage);
            if ("confirmation".equals(node.get("type").asText())) {
                System.out.println(node.get("message").asText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(Socket socket, String message) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(message); // UTF is a string encoding
        out.flush();
    }
    public String receiveMessage(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        return in.readUTF();
    }

    private void listFiles() throws IOException {
        // Listar archivos del servidor
        File folder = new File("server_files/");
        File[] listOfFiles = folder.listFiles();
        List<Map<String, Object>> files = new ArrayList<>();
        System.out.println("1");
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

    private void receiveFile(String filename, String Size) throws IOException {
        File file = new File("server_files/" + filename);
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            int expectedSize = Integer.parseInt(Size);
            byte[] buffer = new byte[4096];
            int read = 0;
            int totalRead = 0;

            while (totalRead < expectedSize && (read = input.read(buffer, 0, Math.min(buffer.length, expectedSize - totalRead))) != -1) {
                bos.write(buffer, 0, read);
                totalRead += read;
                System.out.println("Received " + totalRead + " of " + expectedSize + " bytes.");
            }

            System.out.println("File reception completed.");
            output.writeUTF("Archivo Enviado : ");
        } catch (IOException e) {
            System.out.println("Error receiving file: " + e.getMessage());
            throw e;
        }
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
            output.close();
            input.close();
            clientSocket.close();
            
        } catch (IOException e) {
            System.out.println("Error closing streams or socket: " + e.getMessage());
        }
    }


}
