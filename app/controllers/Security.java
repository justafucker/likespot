package controllers;

import models.User;

public class Security extends Secure.Security {
    static boolean authenticate(String username, String password) {
        return User.connect(username, password) != null;
    }

    static void onAuthenticated() {
        Application.index(null, null, null, null);
    }

    static void onDisconnected() {
        Application.index(null, null, null, null);
    }
}
