package controllers;

import models.Product;
import play.mvc.Controller;

import java.util.List;

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