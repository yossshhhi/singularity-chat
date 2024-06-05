package kz.yossshhhi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class InMemoryAuthProvider implements AuthenticationProvider{
    private class User {
        private String login;
        private String password;
        private String username;

        public User(String login, String password, String username) {
            this.login = login;
            this.password = password;
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    private List<User> list;

    public InMemoryAuthProvider() {
        this.list = new ArrayList<>(Arrays.asList(
                new User("Alex@gmail.com", "111", "Alex"),
                new User("Ben@gmail.com", "111", "Ben"),
                new User("John@gmail.com", "111", "John")
        ));
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for(User u : list) {
            if(u.login.equals(login) && u.password.equals(password)) {
                return u.username;
            }
        }
        return null;
    }

    @Override
    public String changeNick(String oldNick, String newNick) {
        Optional<User> user2Change = list.stream().filter(user -> user.getUsername().equals(oldNick)).findFirst();
        Optional<User> optional = list.stream().filter(user -> user.getUsername().equals(newNick)).findFirst();
        if (optional.isEmpty() && user2Change.isPresent()) {
            User user = user2Change.get();
            user.setUsername(newNick);
            return user.getUsername();
        }
        return oldNick;
    }
}
