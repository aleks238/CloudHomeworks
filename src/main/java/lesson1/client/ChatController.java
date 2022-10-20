package lesson1.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
public class ChatController implements Initializable {
    @FXML
    private ListView<String> listView;
    private IONet net;
    private List<File> files;
    private byte[] buffer = new byte[8192];

    private void addMessage(String message) {
        Platform.runLater(() -> listView.getItems().add(message));
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8489);
            net = new IONet(this::addMessage, socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void chooseFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        files = fileChooser.showOpenMultipleDialog(null);
        files.forEach(file -> listView.getItems().add(String.valueOf(file)));
        System.out.println(files);
    }
    public void sendFile(ActionEvent actionEvent) throws IOException {
        files.forEach(file -> {
            try {
                String filePath = file.toString();
                String[] tokens = filePath.split("\\\\");
                String fileName = tokens[tokens.length - 1];
                net.sendFile(file,fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listView.getItems().clear();
        listView.getItems().add("Файлы отправлены на сервер");
    }

}
