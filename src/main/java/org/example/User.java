package org.example;

public class User {
    private int userId;
    private String login;
    private String password;
    private String role;

    public User(String log, String pas, String sto){
        login = log;
        password = pas;
        role = sto;
    }
    public User(int id, String log, String pas, String sto){
        userId = id;
        login = log;
        password = pas;
        role = sto;
    }

    public int getUserId() { return userId; }
    public String getUserLogin() { return login; }
    public String getUserPassword() { return password; }
    public String getUserRole() { return role; }

    public void setUserLogin(String log){ login = log; }
    public void setUserPassword(String pas){ password = pas; }
    public void setUserRole(String sto){ role = sto; }
}
