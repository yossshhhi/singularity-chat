package kz.yossshhhi.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {

    @FXML
    private TextField msgField, loginField;
    @FXML
    private TextArea msgArea;
    @FXML
    private HBox loginBox, msgBox;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public void setUsername(String username) {
        this.username = username;
        if (this.username == null) {
            loginBox.setVisible(true);
            loginBox.setManaged(true);
            msgBox.setVisible(false);
            msgBox.setManaged(false);
        } else {
            loginBox.setVisible(false);
            loginBox.setManaged(false);
            msgBox.setVisible(true);
            msgBox.setManaged(true);
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    // цикл авторизации
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/login_ok ")) {
                            setUsername(msg.split("\\s+")[1]);
                            break;
                        } else if (msg.startsWith("/login_failed")) {
                            alertMessage("Пользователь с именем " + msg.split("\\s+")[1] + " уже существует");
                        }
                    }
                    // цикл общения
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/bad_request")) {
                            String alertMsg = msg.split("\\s+", 2)[1];
                            alertMessage(alertMsg);
                        } else {
                            msgArea.appendText(msg + "\n");
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to connect to server");
        }
    }

    public void login() {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        if (loginField.getText().isEmpty()) {
            alertMessage("Имя пользователя не может быть пустым");
            return;
        }
        try {
            out.writeUTF("/login " + loginField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        setUsername(null);
        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
        } catch (IOException e) {
            alertMessage("Невозможно отправить сообщение");
        }
    }

    private void alertMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.showAndWait();
        });
    }
}