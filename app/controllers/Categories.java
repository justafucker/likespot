package controllers;

import models.Category;
import play.*;
import play.mvc.*;

@CRUD.For(Category.class)
@With(Secure.class)
public class Categories extends CRUD {
}
