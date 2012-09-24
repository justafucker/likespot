package controllers;

import models.Category;
import models.Product;
import models.User;
import play.Logger;
import play.cache.Cache;
import play.cache.CacheFor;
import play.db.helper.SqlQuery;
import play.mvc.Controller;
import sun.security.krb5.internal.ccache.CCacheInputStream;

import java.io.*;
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
        renderArgs.put("c", c != null ? c : -1); // Override c
        renderArgs.put("p", p != null ? p : -1); // Override c
        renderArgs.put("selectedCategory", c != null ? Category.findById(c) : null);
        renderArgs.put("selectedProduct", p != null ? Product.findById(p) : null);
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
                    String query = "(draft is null or draft is false) and " + categoryCriteria + " or " + parentCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                } else if (c != null && p == null) {
                    String categoryCriteria = "category.id = " + c;
                    String query = "(draft is null or draft is false) and " + categoryCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                } else {
                    String categoryCriteria = "parent.id = " + p;
                    String query = "(draft is null or draft is false) and " + categoryCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                }
                render(products, user);
            } else {
                List<Category> categories = Category.all().fetch();
                if (c == null && p == null) {
                    products = Product.find("order by date desc").fetch();
                } else if (c != null && p == null) {
                    String categoryCriteria = "category.id = " + c;
                    String query = "(draft is null or draft is false) and " + categoryCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                } else {
                    String categoryCriteria = "parent.id = " + p;
                    String query = "(draft is null or draft is false and) " + categoryCriteria + " order by date desc";
                    products = Product.find(query).fetch();
                }
                render(products, categories);
            }
        } else {
            List<Category> categories = Category.all().fetch();
            if (c == null && p == null) {
                products = Product.find("order by date desc").fetch();
            } else if (c != null && p == null) {
                String categoryCriteria = "category.id = " + c;
                String query = "(draft is null or draft is false) and " + categoryCriteria + " order by date desc";
                products = Product.find(query).fetch();
            } else {
                String categoryCriteria = "parent.id = " + p;
                String query = "(draft is null or draft is false) and " + categoryCriteria + " order by date desc";
                products = Product.find(query).fetch();

            }
            render(products, categories);
        }
    }

    private static String safeInlineParams(String prefix, List<Long> params) {
        return !params.isEmpty() ? prefix + SqlQuery.inlineParam(params) : "1 <> 1";
    }

    public static void productPhoto(long id) throws IOException {
        final Product product = Product.findById(id);
        notFoundIfNull(product);
        String contentType = Cache.get("product_photo_type" + id, String.class);
        if (contentType == null) {
            if (product.getPhoto().exists()) {
                contentType = product.getPhoto().type();
                Cache.set("product_photo_type" + id, contentType);
            }
        }
        if (contentType != null) {
            response.setContentTypeIfNotSet(contentType);
            byte[] bytes = (byte[]) Cache.get("product_photo_bytes_" + id);
            if (bytes == null)  {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream is = product.getPhoto().get();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                bytes =  buffer.toByteArray();
                Cache.set("product_photo_bytes_" + id, bytes);
            }
            renderBinary(new ByteArrayInputStream(bytes));
        }
    }

}