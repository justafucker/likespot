package controllers;

import com.google.gson.*;
import models.Category;
import models.NoJSON;
import models.Product;
import models.User;
import play.cache.Cache;
import play.db.helper.SqlQuery;
import play.i18n.Lang;
import play.mvc.Controller;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Application extends Controller {
    public static final int PAGE_SIZE = 15;
    private static final String IS_NOT_DRAFT_CRITERIA = "(draft is null or draft is false)";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d, MMMM yyyy", new Locale(Lang.get()));
    private static final JsonSerializer<Timestamp> JSON_TIMESTAMP_SERIALIZER = new JsonSerializer<Timestamp>() {
        public JsonElement serialize(Timestamp date, Type type, JsonSerializationContext jsonSerializationContext) {
            String dateFormatAsString = DATE_FORMAT.format(date);
            return new JsonPrimitive(dateFormatAsString);
        }
    };
    private static final Gson GSON = new GsonBuilder().
            registerTypeAdapter(Timestamp.class, JSON_TIMESTAMP_SERIALIZER).
            setExclusionStrategies(new ExclusionStrategy() {
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    /**
                     * This is a temporary solution.
                     */
                    return fieldAttributes.getAnnotation(NoJSON.class) != null;
                }

                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            }).
            create();

    public static void getProducts(Long c, Long p, int page) {
        if (Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            List<Product> products = getProducts(user, c, p, page);
            renderJSON(GSON.toJson(products));
        } else {
            List<Product> products = getProducts(null, c, p, page);
            renderJSON(GSON.toJson(products));
        }
    }

    //public static class Suggestion

    public static void suggestProducts(String query) {
        List<Product> products;
        if (query != null || query.trim().length() > 0) {
            products = Product.find("title like " + SqlQuery.inlineParam("%" + query + "%")).fetch(PAGE_SIZE);
        } else {
            products = Collections.EMPTY_LIST;
        }
        renderJSON(GSON.toJson(products));
    }

    private static List<Product> getProducts(User user, Long c, Long p, int page) {
        if (user != null && (c == null || c == -1) && (p == null || p == -1)) {
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
            String query = IS_NOT_DRAFT_CRITERIA + " and (" + categoryCriteria + " or " + parentCriteria + ") order by date desc, id desc";
            return Product.find(query).from(page * PAGE_SIZE).fetch(PAGE_SIZE);
        } else if (c != null && c != -1) {
            String query = IS_NOT_DRAFT_CRITERIA + " and category.id = " + c + " order by date desc, id desc";
            return Product.find(query).from(page * PAGE_SIZE).fetch(PAGE_SIZE);
        } else if (p != null && p != -1) {
            String query = IS_NOT_DRAFT_CRITERIA + " and parent.id = " + p + " order by date desc, id desc";
            return Product.find(query).from(page * PAGE_SIZE).fetch(PAGE_SIZE);
        } else {
            return Product.find("draft is null or draft is false order by date desc, id desc").from(page * PAGE_SIZE).fetch(PAGE_SIZE);
        }
    }

    public static void index(Long c, Long p) {
        renderArgs.put("home", true);
        renderArgs.put("c", c != null ? c : -1); // Override c
        renderArgs.put("p", p != null ? p : -1); // Override p
        renderArgs.put("selectedCategory", c != null ? Category.findById(c) : null);
        renderArgs.put("selectedProduct", p != null ? Product.findById(p) : null);
        User user = Security.isConnected() ? (User) User.find("byEmail", Security.connected()).first() : null;
        if (user != null) {List<Long> pp = new ArrayList<Long>(user.products.size());
            for (Product product : user.products) {
                pp.add(product.getId());
            }
            renderArgs.put("userProducts", !pp.isEmpty() ? SqlQuery.inlineParam(pp).replace('(', '[').replace(')', ']') : "[]");
            List<Product> products = getProducts(user, c, p, 0);
            render(products, user);
        } else {
            renderArgs.put("userProducts", "[]");
            List<Category> categories = Category.all().fetch();
            List<Product> products = getProducts(user, c, p, 0);
            render(products, categories);
        }
    }

    private static String safeInlineParams(String prefix, List<Long> params) {
        return !params.isEmpty() ? prefix + SqlQuery.inlineParam(params) : "1 <> 1";
    }

    public static void toggleLike(long id) {
        if (Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            Product product = Product.findById(id);
            if (!user.products.contains(product)) {
                user.products.add(product);
            } else {
                user.products.remove(product);
            }
            user.save();
        }
    }

    public static void productPhoto(long id) throws IOException {
        final Product product = Product.findById(id);
        notFoundIfNull(product);
        String contentType = Cache.get("product_photo_type_" + id, String.class);
        if (contentType == null) {
            if (product.getPhoto().exists()) {
                contentType = product.getPhoto().type();
                Cache.set("product_photo_type_" + id, contentType);
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