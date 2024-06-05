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
    private AuthenticationProvider authenticationProvider;

    public Server(int port) {
        this.port = port;
        this.list = new ArrayList<>();
        this.authenticationProvider = new InMemoryAuthProvider();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port + ". Ожидаем подключение клиента...");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String msg) {
        for (ClientHandler clientHandler : list) {
            clientHandler.sendMessage(msg);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        list.add(clientHandler);
        sendClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        list.remove(clientHandler);
        sendClientList();
    }

    public boolean existsByUsername(String username) {
        return list.stream().anyMatch(clientHandler -> clientHandler.getUsername() != null && clientHandler.getUsername().equals(username));
    }

    public boolean sendPrivateMessage(ClientHandler sender, String recipient, String msg) {
        Optional<ClientHandler> optional = list.stream().filter(clientHandler -> clientHandler.getUsername().equals(recipient)).findFirst();
        if (optional.isPresent()) {
            ClientHandler clientHandler = optional.get();
            clientHandler.sendMessage("Сообщение от " + sender.getUsername() + ": " + msg);
            sender.sendMessage("Сообщение для " + recipient + ": " + msg);
            return false;
        }
        return true;
    }

    public void sendClientList() {
        StringBuilder builder = new StringBuilder("/clients_list ");
        for(ClientHandler c : list) {
            builder.append(c.getUsername()).append(" ");
        }
        builder.setLength(builder.length() - 1);
        String clientList = builder.toString();
        for(ClientHandler c : list) {
            c.sendMessage(clientList);
        }
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }
}
