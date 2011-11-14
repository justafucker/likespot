package controllers;

import play.*;
import play.data.validation.Min;
import play.data.validation.Required;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        List<Product> products = Product.findAll();
        render(products);
    }

    public static void productPhoto(long id) {
       final Product product = Product.findById(id);
       notFoundIfNull(product);
       if (product.getPhoto().exists()) {
           response.setContentTypeIfNotSet(product.getPhoto().type());
           renderBinary(product.getPhoto().get());
       }
    }

}