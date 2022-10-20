package lesson1.server;

import java.io.*;
import java.net.Socket;
public class ClientHandler implements Runnable {
    private boolean running;
    private final byte[] buffer;
    private final InputStream inputStream;
    private final DataInputStream dataInputStream;
    private final OutputStream outputStream;
    private final Socket socket;

    ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        running = true;
        buffer = new byte[8162];
    }
    public void stop() {
        running = false;
    }
    @Override
    public void run() {
        try {
            while (running) {
                String fileName = dataInputStream.readUTF();
                int length;
                try (FileOutputStream fileOutputStream = new FileOutputStream("files/" + fileName)) {
                    while ((length = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                }

            }
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void closeConnection() throws IOException {
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}
