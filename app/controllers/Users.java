package controllers;

import models.Category;
import models.User;
import play.mvc.Before;
import play.mvc.With;

import javax.persistence.OneToMany;
import java.util.List;

@CRUD.For(User.class)
@With(Secure.class)
public class Users extends CRUD {
    @Before
    public static void checkUser() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            renderArgs.put("user", user);
        }
    }
}
