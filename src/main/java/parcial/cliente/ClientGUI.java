
package parcial.cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.text.Document;

public class ClientGUI extends JFrame {
    private final JTextField hostField = new JTextField("localhost", 20);
    private final JTextField portField = new JTextField("12345", 5);
    private final JTextArea logArea = new JTextArea(10, 50);
    private final JButton connectButton = new JButton("Connect");
    private final JButton listFilesButton = new JButton("List Files");
    private final JButton sendFileButton = new JButton("Send File");
    private final JButton receiveFileButton = new JButton("Receive File");
    private JFileChooser fileChooser = new JFileChooser();
    private cliente fileClient;

    public ClientGUI() {
        super("File Client GUI");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

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
                logArea.append("Connected to server\n");
            } catch (IOException ioException) {
                logArea.append("Failed to connect: " + ioException.getMessage() + "\n");
            }
        });

        listFilesButton.addActionListener(e -> {
            try {
                fileClient.listFiles();
            } catch (IOException ioException) {
                logArea.append("Error listing files: " + ioException.getMessage() + "\n");
            }
        });

       sendFileButton.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(ClientGUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    // Aquí utilizamos getAbsolutePath() en lugar de getName()
                    fileClient.sendFile(file.getAbsolutePath()); // Asegúrate de que el método sendFile maneje la ruta completa
                    logArea.append("File sent: " + file.getName() + "\n");
                } catch (IOException ioException) {
                    logArea.append("Error sending file: " + ioException.getMessage() + "\n");
                }
            }
        });
       

        receiveFileButton.addActionListener(e -> {
            // Implement receiving a file from the server
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI frame = new ClientGUI();
            frame.setVisible(true);
        });
    }
}
