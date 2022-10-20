package lesson1.client;

import java.io.*;
import java.net.Socket;
public class IONet implements Closeable {
    private final Callback callback;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final DataOutputStream dataOutputStream;
    private final byte[] buffer;

    public IONet(Callback callback, Socket socket) throws IOException {
        this.callback = callback;
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        buffer = new byte[8192];
        Thread readingThread = new Thread(this::readMessages);
        readingThread.setDaemon(true);
        readingThread.start();
    }

    public void sendFile(File file, String fileName) throws IOException {
        dataOutputStream.writeUTF(fileName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
    private void readMessages() {
        try {
            while (true) {
                int length = inputStream.read(buffer);
                String message = new String(buffer, 0, length).trim();
                callback.onReceive(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}
