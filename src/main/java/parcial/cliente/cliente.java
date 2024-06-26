
package parcial.cliente;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class cliente {
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private ObjectMapper objectMapper;

    public cliente(String host, int port) throws IOException {
        socket = new Socket(host, port);
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
        objectMapper = new ObjectMapper();
    }

    public void listFiles() throws IOException {
        // Enviar comando para listar archivos
        Map<String, String> command = new HashMap<>();
        command.put("type", "list_files");
        sendCommand(command);
    }

    public void listClients() throws IOException {
        Map<String, String> command = new HashMap<>();
        command.put("type", "list_clients");
        sendCommand(command);
    }
    

    public void sendFile(String filePath) throws IOException {
        // Enviar comando para enviar un archivo
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);   
        }

        Map<String, String> command = new HashMap<>();
        command.put("type", "receive_file");
        command.put("filename", file.getName());
        command.put("filesize", String.valueOf(file.length()));
        sendCommand(command);

        // Enviar el archivo
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        fileInputStream.close();
    }
    public void sendMessage(Socket socket, String message) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(message); // UTF is a string encoding
        out.flush();
    }
    public String receiveMessage() throws IOException {
        return input.readUTF();
    }
    
public void receiveFile(String filename) throws IOException {
    // Enviar comando para recibir un archivo
    Map<String, String> command = new HashMap<>();
    command.put("type", "send_file");
    command.put("filename", filename);
    sendCommand(command);

    // Leer la respuesta del servidor antes de intentar recibir el archivo
    String serverResponse = input.readUTF();
    if (!"File found".equals(serverResponse)) {
        System.out.println("File not found on server.");
        return;
    }

    // Recibir archivo del servidor
    long fileSize = input.readLong(); // Asegúrate de que el servidor envíe esto antes del archivo
    File localFile = new File("local_folder/" + filename);
    try (FileOutputStream fos = new FileOutputStream(localFile);
         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalRead = 0;

        while (totalRead < fileSize) {
            bytesRead = input.read(buffer, 0, buffer.length);
            if (bytesRead == -1) {
                break; // Salir si no hay más datos para leer
            }
            bos.write(buffer, 0, bytesRead);
            totalRead += bytesRead;
        }

    } // El try-with-resources cierra automáticamente los streams

    System.out.println("File received successfully. Total bytes read: " + fileSize);
}


    private void sendCommand(Map<String, String> command) throws IOException {
        String jsonString = objectMapper.writeValueAsString(command);
        output.writeUTF(jsonString);
        
        output.flush();
    }

    public void closeConnection() throws IOException {
        input.close();
        output.close();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            cliente client = new cliente("localhost", 12345);
            client.sendFile("example.txt");
            client.listFiles();     
            client.receiveFile("example.txt");
            client.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

