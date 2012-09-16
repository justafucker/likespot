package controllers;

import models.Category;
import models.User;
import play.mvc.With;

import javax.persistence.OneToMany;
import java.util.List;

@CRUD.For(User.class)
@With(Secure.class)
public class Users extends CRUD {
    @OneToMany
    public List<Category> categories;
}
