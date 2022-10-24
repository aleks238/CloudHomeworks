package lesson2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class Server {
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final ByteBuffer buffer;
    private final StringBuilder name;

    public Server(int port) throws IOException {
        buffer = ByteBuffer.allocate(100);
        name = new StringBuilder();
        serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (serverChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            try {
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept();
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void handleAccept() throws IOException {
        System.out.println("Client accepted");
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder message = new StringBuilder();
        while (true) {
            int read = channel.read(buffer);
            if (read == -1) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                message.append((char) buffer.get());
            }
            buffer.clear();
        }
        commands(channel, message.toString());
    }
    private void commands(SocketChannel channel, String message) throws IOException {
        if (message.startsWith("mkdir ")) {
            if (!(createDirectory(message))) {
                channel.write(ByteBuffer.wrap("Директория уже существует".getBytes(StandardCharsets.UTF_8)));
            }
        } else if (message.startsWith("touch ")) {
            if (!(createFile(message))) {
                channel.write(ByteBuffer.wrap("Файл уже существует".getBytes(StandardCharsets.UTF_8)));
            }
        } else if (message.startsWith("cat ")) {
            channel.write(ByteBuffer.wrap(readFile(message + "\n").getBytes(StandardCharsets.UTF_8)));
        } else if (message.startsWith("ls")) {
            Set<String> files = listFiles();
            files.forEach(file -> {
                try {
                    channel.write(ByteBuffer.wrap(file.getBytes(StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    private boolean createDirectory(String message) throws IOException {
        String[] tokens = message.split("\\s+");
        for (int i = 1; i < tokens.length; i++) {
            name.append(tokens[i]).append(" ");
        }
        String currentDirectory = Paths.get("").toAbsolutePath().toString();
        Path path = Paths.get(currentDirectory + "/" + name.toString().trim());
        if (!(Files.exists(path))) {
            Files.createDirectory(path);
            return true;
        }
        return false;
    }
    private boolean createFile(String message) throws IOException {
        String[] tokens = message.split("\\s+");
        for (int i = 1; i < tokens.length; i++) {
            name.append(tokens[i]).append(" ");
        }
        String currentDirectory = Paths.get("").toAbsolutePath().toString();
        Path path = Paths.get(currentDirectory + "/" + name.toString().trim());
        if (!(Files.exists(path))) {
            Files.createFile(path);
            return true;
        }
        return false;
    }
    private String readFile(String message) throws IOException {
        String[] tokens = message.split("\\s+");
        for (int i = 1; i < tokens.length; i++) {
            name.append(tokens[i]).append(" ");
        }
        String currentDirectory = Paths.get("").toAbsolutePath().toString();
        Path path = Paths.get(currentDirectory + "/" + name.toString().trim());
        String read = Files.readAllLines(path).get(0);
        return read;
    }
    private Set<String> listFiles() throws IOException {
        Path currentDirectory = Paths.get("").toAbsolutePath();
        try (Stream<Path> stream = Files.list(currentDirectory)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }
    public static void main(String[] args) throws IOException {
        new Server(8189);
    }
}
