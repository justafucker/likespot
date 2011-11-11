package controllers;

import models.Product;
import play.*;
import play.mvc.*;

@CRUD.For(Product.class)
public class Products extends CRUD {
}
