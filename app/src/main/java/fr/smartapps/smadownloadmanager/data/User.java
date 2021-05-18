package fr.smartapps.smadownloadmanager.data;

/**
 * Created by vincentchann on 17/12/2016.
 */

public class User {

    public String username;
    public String password;
    public String PUBLISHER_TOKEN;

    public User() {
    }

    public User(String username, String password, String PUBLISHER_TOKEN) {
        this.username = username;
        this.password = password;
        this.PUBLISHER_TOKEN = PUBLISHER_TOKEN;
    }

    public String string() {
        return "username:" + this.username + "\npassword:" + this.password + "\nPUBLISHER_TOKEN:" + this.PUBLISHER_TOKEN;
    }
}
