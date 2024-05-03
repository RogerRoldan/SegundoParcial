
package parcial.cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.text.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientGUI extends JFrame {
    private final JTextField hostField = new JTextField("localhost", 20);
    private final JTextField portField = new JTextField("12345", 5);
    private final JTextArea logArea = new JTextArea(10, 50);
    private final JButton connectButton = new JButton("Connect");
    private final JButton listFilesButton = new JButton("List Files");
    private final JButton listClientsButton = new JButton("List Clients");
    private final JButton sendFileButton = new JButton("Send File");
    private final JButton receiveFileButton = new JButton("Receive File");
    private final JTextField NameField = new JTextField("", 20);
    private JFileChooser fileChooser = new JFileChooser();
    private cliente fileClient;

    public ClientGUI() {
        super("File Client GUI");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);

        JPanel northPanel = new JPanel();
        northPanel.add(new JLabel("Host:"));
        northPanel.add(hostField);
        northPanel.add(new JLabel("Port:"));
        northPanel.add(portField);
        northPanel.add(connectButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.add(listFilesButton);
        southPanel.add(sendFileButton);
        southPanel.add(listClientsButton);
        southPanel.add(new JLabel("Namefile:"));
        southPanel.add(NameField);
        southPanel.add(receiveFileButton);
        

        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        setupActions();
    }

    private void setupActions() {
        connectButton.addActionListener(e -> {
            try {
                fileClient = new cliente(hostField.getText(), Integer.parseInt(portField.getText()));
                String serverResponse = fileClient.receiveMessage();  // Asume que tienes un método para recibir mensajes del servidor
                connectButton.setEnabled(false);
                logArea.append(serverResponse + "\n");
                if(serverResponse.equals("Connection refused: maximum connections reached.")){
                   connectButton.setEnabled(true); 
                }
                
            } catch (IOException ioException) {
                connectButton.setEnabled(true);
                logArea.append("Failed to connect: " + ioException.getMessage() + "\n");
            }
        });

        listFilesButton.addActionListener((ActionEvent e) -> {
            try {
                fileClient.listFiles();  // Envia el comando para listar los archivos
                String serverResponse = fileClient.receiveMessage();  // Recibe el JSON como string

                // Limpiar el área de texto antes de agregar nuevo contenido
                logArea.setText("");

                // Parsear el JSON y formatear la salida
                ObjectMapper mapper = new ObjectMapper();
                JsonNode filesArray = mapper.readTree(serverResponse);
                int i = 1;
                if (filesArray.isArray()) {
                    for (JsonNode fileNode : filesArray) {
                        String fileName = fileNode.path("name").asText();
                        String fileSize = fileNode.path("size").asText();
                        String fileExtension = fileNode.path("extension").asText();
                        logArea.append( i + " <----  Nombre: " + fileName + ",  ----  Tamaño: " + fileSize + "  ------ bytes, Extension: " + fileExtension + "\n");
                        i++;
                    }
                }
            } catch (IOException ioException) {
                logArea.append("Error listing files: " + ioException.getMessage() + "\n");
            }
        });

        listClientsButton.addActionListener(e -> {
            try {
                fileClient.listClients();  // Envía el comando para listar los clientes
                String serverResponse = fileClient.receiveMessage();  // Recibe la respuesta del servidor
        
                // Limpiar el área de texto antes de agregar nuevo contenido
                logArea.setText("");
        
                // Agregar la respuesta del servidor al área de texto
                logArea.append("Clientes Conectados:\n" + serverResponse);
            } catch (IOException ioException) {
                logArea.append("Error listing clients: " + ioException.getMessage() + "\n");
            }
        });
        


       sendFileButton.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(ClientGUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    // Aquí utilizamos getAbsolutePath() en lugar de getName()
                    fileClient.sendFile(file.getAbsolutePath()); // Asegúrate de que el método sendFile maneje la ruta completa
                    String serverResponse = fileClient.receiveMessage();
                    logArea.append(serverResponse + file.getName() + "   se ha subido correctamente \n");
                } catch (IOException ioException) {
                    logArea.append("Error sending file: " + ioException.getMessage() + "\n");
                }
            }
        });
       

        receiveFileButton.addActionListener(e -> {
            String filename = NameField.getText();
            new Thread(() -> {  // Usar un nuevo hilo para no bloquear la UI
                try {
                    fileClient.receiveFile(filename);
                    SwingUtilities.invokeLater(() -> {  // Utilizar SwingUtilities.invokeLater para interactuar con la UI
                        logArea.append("Archivo recibido correctamente: " + filename + "\n");
                    });
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        logArea.append("Error al recibir el archivo: " + ex.getMessage() + "\n");
                    });
                }
            }).start();
        });


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
                
        String dirPathlocal = "local_folder/";
        createDirectoryIfNotExists(dirPathlocal);
        
        SwingUtilities.invokeLater(() -> {
            ClientGUI frame = new ClientGUI();
            frame.setVisible(true);
        });
    }
}
