package controllers;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.google.gson.*;
import models.Category;
import models.NoJSON;
import models.Product;
import models.User;
import play.Logger;
import play.cache.Cache;
import play.db.helper.SqlQuery;
import play.db.jpa.JPA;
import play.i18n.Lang;
import play.modules.s3blobs.S3Blob;
import play.mvc.Controller;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
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

    public static void getProducts(Long c, Long p, Long u, String s, int page) {
        if (Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            List<Product> products = getProducts(user, c, p, u, s, page);
            renderJSON(GSON.toJson(products));
        } else {
            List<Product> products = getProducts(null, c, p, u, s, page);
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

    private static List<Product> getProducts(User user, Long c, Long p, Long u, String s, int page) {
        if (u != null && u != -1) {
            String query = "select product from User as user inner join user.products as product where user.id = " + u;
            if (c != null && c != -1) {
                query += " and product.category.id = " + c;
            }
            if (s != null && s.length() > 0) {
                query += " and (title like '%" + s.replaceAll("\'", "''") + "%' or description like '%" + s.replaceAll("\'", "''") + "%')";
            }
            query += " order by product.date desc, product.id desc";
            return (List<Product>) JPA.em().createQuery(query).setMaxResults(PAGE_SIZE).setFirstResult(page * PAGE_SIZE).getResultList();
        } else if (user != null && (c == null || c == -1) && (p == null || p == -1)) {
            List<Long> categories = new ArrayList<Long>(user.categories.size());
            for (Category category : user.categories) {
                if (!category.isDraft()) {
                    categories.add(category.getId());
                }
            }
            List<Long> parents = new ArrayList<Long>(user.products.size());
            for (Product product : user.products) {
                parents.add(product.getId());
            }
            String categoryCriteria = safeInlineParams("category.id in ", categories);
            String parentCriteria = safeInlineParams("parent.id in ", parents);
            String searchCriteria = "";
            if (s != null && s.length() > 0) {
                searchCriteria += " and (title like '%" + s.replaceAll("\'", "''") + "%' or description like '%" + s.replaceAll("\'", "''") + "%')";
            }
            String query = IS_NOT_DRAFT_CRITERIA + " and (" + categoryCriteria + " or " + parentCriteria + ")" + searchCriteria + " order by date desc, id desc";
            return Product.find(query).from(page * PAGE_SIZE).fetch(PAGE_SIZE);
        } else if (c != null && c != -1) {
            String query = IS_NOT_DRAFT_CRITERIA + " and category.id = " + c + " and (category.draft is null or category.draft = false) order by date desc, id desc";
            return Product.find(query).from(page * PAGE_SIZE).fetch(PAGE_SIZE);
        } else if (p != null && p != -1) {
            String query = IS_NOT_DRAFT_CRITERIA + " and parent.id = " + p + " and (category.draft is null or category.draft = false) order by date desc, id desc";
            return Product.find(query).from(page * PAGE_SIZE).fetch(PAGE_SIZE);
        } else {
            String searchCriteria = "";
            if (s != null && s.length() > 0) {
                searchCriteria += " and (title like '%" + s.replaceAll("\'", "''") + "%' or description like '%" + s.replaceAll("\'", "''") + "%')";
            }
            return Product.find(IS_NOT_DRAFT_CRITERIA + " and (category.draft is null or category.draft = false)" + searchCriteria + " order by date desc, id desc").from(page * PAGE_SIZE).fetch(PAGE_SIZE);
        }
    }

    public static void index(Long c, Long p, Long u, String s) {
        renderArgs.put("home", true);
        renderArgs.put("c", c != null ? c : -1); // Override c
        renderArgs.put("p", p != null ? p : -1); // Override p
        renderArgs.put("u", u != null ? u : -1); // Override u
        renderArgs.put("s", s != null ? s : ""); // Override u
        renderArgs.put("selectedCategory", c != null ? Category.findById(c) : null);
        renderArgs.put("selectedProduct", p != null ? Product.findById(p) : null);
        renderArgs.put("selectedUser", u != null ? User.findById(u) : null);
        User user = Security.isConnected() ? (User) User.find("byEmail", Security.connected()).first() : null;
        if (u != null && u != -1) {
            List<Product> products = getProducts(user, c, null, u, s, 0);
            if (user != null) {
                List<Long> pp = new ArrayList<Long>(user.products.size());
                for (Product product : user.products) {
                    pp.add(product.getId());
                }
                renderArgs.put("userProducts", !pp.isEmpty() ? SqlQuery.inlineParam(pp).replace('(', '[').replace(')', ']') : "[]");
                render(products, user);
            } else {
                List<Category> categories = Category.find(IS_NOT_DRAFT_CRITERIA).fetch();
                render(products, categories);
            }
        } else if (user != null) {
            List<Long> pp = new ArrayList<Long>(user.products.size());
            for (Product product : user.products) {
                pp.add(product.getId());
            }
            renderArgs.put("userProducts", !pp.isEmpty() ? SqlQuery.inlineParam(pp).replace('(', '[').replace(')', ']') : "[]");
            List<Product> products = getProducts(user, c, p, null, s, 0);
            render(products, user);
        } else {
            renderArgs.put("userProducts", "[]");
            List<Category> categories = Category.find(IS_NOT_DRAFT_CRITERIA).fetch();
            List<Product> products = getProducts(user, c, p, null, s, 0);
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

    public static void toggleFollow(long id) {
        if (Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            User friend = User.findById(id);
            if (!user.friends.contains(friend)) {
                user.friends.add(friend);
            } else {
                user.friends.remove(friend);
            }
            user.save();
        }
    }

    public static void productPhoto(long id) throws IOException {
        final Product product = Product.findById(id);
        notFoundIfNull(product);
        try {
            if (!product.hasThumbnail() && product.hasPhoto() && product.getPhoto().exists()) {
                BufferedImage original = ImageIO.read(product.getPhoto().get());
                int size = Math.min(original.getWidth(), original.getHeight());
                BufferedImage cropped = original.getSubimage((original.getWidth() - size) / 2,
                        (original.getHeight() - size) / 2, size, size);
                Image thumbnailImage = cropped.getScaledInstance(Products.THUMBNAIL_SIZE, Products.THUMBNAIL_SIZE, Image.SCALE_SMOOTH);
                BufferedImage thumbnail = new BufferedImage(Products.THUMBNAIL_SIZE, Products.THUMBNAIL_SIZE, BufferedImage.TYPE_INT_RGB);
                Graphics graphics = thumbnail.createGraphics();
                graphics.drawImage(thumbnailImage, 0, 0, new Color(0, 0, 0), null);
                graphics.dispose();
                final ByteArrayOutputStream thumbnailOutput = Products.getImageAsStream(thumbnail);
                product.getThumbnail().set(new ByteArrayInputStream(thumbnailOutput.toByteArray(), 0, thumbnailOutput.size()),
                        "image/png");
                product.save();
                Logger.debug("Product #" + product.getId() + " updated.");
            }
        } catch (IOException e) {
            Logger.error("Error while updating thumbnail", e);
        } catch (AmazonS3Exception e) {
            Logger.error("Error while updating thumbnail", e);
        }
        String contentType = Cache.get("product_photo_type_" + id, String.class);
        if (contentType == null) {
            if (product.getThumbnail().exists()) {
                contentType = product.getThumbnail().type();
                Cache.set("product_photo_type_" + id, contentType);
            }
        }
        if (contentType != null) {
            response.setContentTypeIfNotSet(contentType);
            byte[] bytes = (byte[]) Cache.get("product_photo_bytes_" + id);
            if (bytes == null)  {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream is = product.getThumbnail().get();
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

    public static void userPhoto(long id) throws IOException, NoSuchFieldException, IllegalAccessException {
        final User user = User.findById(id);
        notFoundIfNull(user);
        String contentType = Cache.get("user_photo_type_" + id, String.class);
        if (contentType == null) {
            Field field = S3Blob.class.getDeclaredField("key");
            field.setAccessible(true);
            Object key = field.get(user.photo);
            if (key != null && !key.equals("null") && user.photo.exists()) {
                contentType = user.photo.type();
                Cache.set("user_photo_type_" + id, contentType);
            }
        }
        if (contentType != null) {
            response.setContentTypeIfNotSet(contentType);
            byte[] bytes = (byte[]) Cache.get("user_photo_bytes_" + id);
            if (bytes == null)  {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream is = user.photo.get();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                bytes =  buffer.toByteArray();
                Cache.set("user_photo_bytes_" + id, bytes);
            }
            renderBinary(new ByteArrayInputStream(bytes));
        } else {
            renderBinary(new File("public/images/noimage.gif"));
        }
    }

}