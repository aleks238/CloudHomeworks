package lesson1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class ServerIO {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8489);
        System.out.println("Server started...");
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("Client connected");
                new Thread(new ClientHandler(socket)).start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
