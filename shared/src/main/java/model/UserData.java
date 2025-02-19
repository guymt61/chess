package model;

public class UserData {

    public UserData(String username, String password, String email) {
        myUsername = username;
        myPassword = password;
        myEmail = email;
    }

    public String getUsername() {
        return myUsername;
    }

    public void setUsername(String newUsername) {
        myUsername = newUsername;
    }

    public String getPassword() {
        return myPassword;
    }

    public void setPassword(String newPassword) {
        myPassword = newPassword;
    }

    public String getEmail() {
        return myEmail;
    }

    public void setEmail(String newEmail) {
        myEmail = newEmail;
    }

    private String myUsername;
    private String myPassword;
    private String myEmail;
}
