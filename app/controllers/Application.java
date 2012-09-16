package controllers;

import models.Product;
import models.User;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.List;

public class Application extends Controller {
    @Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            if (user != null) {
                renderArgs.put("user", user.fullname);
            }
        }
    }

    public static void index() {
//        User user = User.find("byEmail", Security.connected()).first();
//        List<Product> products;
//        if (user != null) {
//            products = Product.find("where order by date desc").fetch();
//        } else {
//            products = Product.find("order by date desc").fetch();
//        }
//        render(products);
        List<Product> products = Product.find("order by date desc").fetch();
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