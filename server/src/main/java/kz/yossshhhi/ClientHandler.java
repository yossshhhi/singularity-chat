package kz.yossshhhi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String username;
    private int msgCounter;

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/login ")) {
                        String[] tokens = msg.split("\\s+", 3);
                        String login = tokens[1];
                        String password = tokens[2];

                        String nick = server.getAuthenticationProvider()
                                .getUsernameByLoginAndPassword(login, password);
                        if (nick == null) {
                            sendMessage("/login_failed Некорректный логин или пароль");
                            continue;
                        }

                        if (server.existsByUsername(nick)) {
                            sendMessage("/login_failed " + "Пользователь с именем " + nick + " уже в сети");
                        } else {
                            username = nick;
                            sendMessage("/login_ok " + username);
                            server.subscribe(this);
                            break;
                        }
                    }
                }
                while (true) {
                    String msg = in.readUTF();
                    if (msgHandler(msg)) {
                        server.broadcastMessage(username + ": " + msg);
                        msgCounter++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();

    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            disconnect();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean msgHandler(String msg) {
        if (msg.startsWith("/p ")) {
            privateMsgHandler(msg);
            return false;
        } else if (msg.equals("/who_am_i")) {
            sendMessage("Ваше имя пользователя: " + username);
            return false;
        } else if (msg.equals("/stat")) {
            sendMessage("Количество сообщений - " + msgCounter);
            return false;
        }
        return true;
    }

    private void privateMsgHandler(String msg) {
        String[] split = msg.split("\\s+", 3);
        if (split.length < 3) {
            sendMessage("/bad_request Некорректный ввод данных");
            return;
        }
        String recipient = split[1];
        String message = split[2];
        if (server.sendPrivateMessage(this, recipient, message)) {
            sendMessage("/bad_request Пользователь не найден");
        } else {
            msgCounter++;
        }
    }

    public String getUsername() {
        return username;
    }
}
