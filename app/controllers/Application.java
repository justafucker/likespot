package controllers;

import models.Category;
import models.Product;
import models.User;
import play.db.helper.SqlQuery;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.List;

public class Application extends Controller {
    /*@Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            if (user != null) {
                renderArgs.put("user", user.fullname);
            }
        }
    }*/

    public static void index(Long c, Long p) {
        List<Product> products;
        if (Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            if (user != null) {
                if (c == null && p == null) {
                    List<Long> categories = new ArrayList<Long>(user.categories.size());
                    for (Category category : user.categories) {
                        categories.add(category.getId());
                    }
                    List<Long> parents = new ArrayList<Long>(user.products.size());
                    for (Product product : user.products) {
                        parents.add(product.getId());
                    }
                    String categoryCriteria = safeInlineParams("category.id in ", categories);
                    String parentCriteria = safeInlineParams("parent.id in ", parents);
                    String query = categoryCriteria + " or " + parentCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                } else if (c != null && p == null) {
                    String categoryCriteria = "category.id = " + c;
                    String query = categoryCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                } else {
                    String categoryCriteria = "parent.id = " + p;
                    String query = categoryCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                }
                render(products, user);
            } else {
                if (c == null && p == null) {
                    products = Product.find("order by date desc").fetch();
                    List<Category> categories = Category.all().fetch();
                    render(products, categories);
                } else if (c != null && p == null) {
                    String categoryCriteria = "category.id = " + c;
                    String query = categoryCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                } else {
                    String categoryCriteria = "parent.id = " + p;
                    String query = categoryCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                }
            }
        } else {
            List<Category> categories = Category.all().fetch();
            if (c == null && p == null) {
                products = Product.find("order by date desc").fetch();
                render(products, categories);
            } else if (c != null && p == null) {
                String categoryCriteria = "category.id = " + c;
                String query = categoryCriteria + " order by date desc";
                products = Product.find(query).fetch();
            } else {
                String categoryCriteria = "parent.id = " + p;
                String query = categoryCriteria + " order by date desc";
                products = Product.find(query).fetch();

            }
            render(products, categories);
        }
    }

    private static String safeInlineParams(String prefix, List<Long> params) {
        return !params.isEmpty() ? prefix + SqlQuery.inlineParam(params) : "1 <> 1";
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