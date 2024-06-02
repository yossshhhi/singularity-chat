package kz.yossshhhi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Server {
    private int port;
    private List<ClientHandler> list;

    public Server(int port) {
        this.port = port;
        this.list = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port + ". Ожидаем подключение клиента...");
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(this, socket));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String msg) throws IOException {
        for (ClientHandler clientHandler : list) {
            clientHandler.sendMessage(msg);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        list.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        list.remove(clientHandler);
    }

    public boolean existsByUsername(String username) {
        return list.stream().anyMatch(clientHandler -> clientHandler.getUsername() != null && clientHandler.getUsername().equals(username));
    }

    public boolean sendPrivateMessage(String sender, String recipient, String msg) throws IOException {
        Optional<ClientHandler> optional = list.stream().filter(clientHandler -> clientHandler.getUsername().equals(recipient)).findFirst();
        if (optional.isPresent()) {
            ClientHandler clientHandler = optional.get();
            clientHandler.sendMessage("Сообщение от " + sender + ": " + msg);
            return false;
        }
        return true;
    }
}
