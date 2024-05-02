
package parcial.server;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DocumentManager {
    private final File documentDirectory;

    public DocumentManager(String directoryPath) {
        this.documentDirectory = new File(directoryPath);
        if (!documentDirectory.exists()) {
            documentDirectory.mkdirs();
        }
    }

    public List<Document> listDocuments() {
        List<Document> documents = new ArrayList<>();
        File[] files = documentDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    documents.add(new Document(file.getName(), file.length(), getFileExtension(file)));
                }
            }
        }
        return documents;
    }

    public void sendDocument(String filename, DataOutputStream outputStream) throws IOException {
        File file = new File(documentDirectory, filename);
        if (file.exists()) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[4096];
            int count;
            while ((count = bis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
            }
            bis.close();
            outputStream.flush();
        } else {
            throw new FileNotFoundException("File not found: " + filename);
        }
    }

    public void receiveDocument(String filename, DataInputStream inputStream) throws IOException {
        File file = new File(documentDirectory, filename);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buffer = new byte[4096];
        int count;
        while ((count = inputStream.read(buffer)) > 0) {
            bos.write(buffer, 0, count);
        }
        bos.close();
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }
}

class Document {
    private String name;
    private long size;
    private String extension;

    public Document(String name, long size, String extension) {
        this.name = name;
        this.size = size;
        this.extension = extension;
    }

    // Getters
    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getExtension() {
        return extension;
    }
}
