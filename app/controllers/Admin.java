package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

@With(Secure.class)
public class Admin extends Controller {

    public static void index() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            render(user);
        }
    }

}