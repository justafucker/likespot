package controllers;

import models.Category;
import models.Product;
import models.User;
import play.db.helper.SqlQuery;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        User user = User.find("byEmail", Security.connected()).first();
        List<Product> products;
        if (user != null) {
            List<Long> categories = new ArrayList<Long>(user.categories.size());
            for (Category category : user.categories) {
                categories.add(category.getId());
            }
            List<Long> parents = new ArrayList<Long>(user.products.size());
            for (Product product : user.products) {
                parents.add(product.getId());
            }
            products = Product.find(inlineList("category.id in ", categories) + inlineList(" and parent is null or parent.id in ", parents) +
                    " order by date desc").fetch();
        } else {
            products = Product.find("order by date desc").fetch();
        }
        render(products);
    }

    private static String inlineList(String prefix, List<Long> parents) {
        return (!parents.isEmpty() ? prefix + SqlQuery.inlineParam(parents) : "");
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