package kz.yossshhhi;

public interface AuthenticationProvider {
    String getUsernameByLoginAndPassword(String login, String password);
}
