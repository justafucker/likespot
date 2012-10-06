package controllers;

import models.Category;
import models.Product;
import models.User;
import org.apache.commons.mail.SimpleEmail;
import play.libs.Mail;
import play.mvc.*;

import java.util.List;

@CRUD.For(Product.class)
@With(Secure.class)
public class Products extends CRUD {

    @After(only = {"save", "create"})
    public static void sendNotifications(String id) throws Exception {
        Product product = Product.findById(Long.parseLong(id));
        notFoundIfNull(product);
        Category category = product.getCategory();
        if (category != null) {
            List<User> moderators = category.getModerators();
            if (moderators == null && category.getParent() != null)
                moderators = category.getParent().getModerators();
            if (moderators != null && !moderators.isEmpty()) {
                for (User user : moderators)
                    sendEmail(product, user);
            }

        }
    }

    // TODO: actually there is difference between update and creation. We have to send different messages.
    private static void sendEmail(Product product, User user) throws Exception {
        SimpleEmail email = new SimpleEmail();
        email.addTo(user.email);
        email.setFrom("noreply@likespot.ru", "Likespot");
        email.setSubject("Project in a category you moderate has been updated");
        email.setCharset("UTF-8");
        Mail.send(email.setMsg("Product " + product.getTitle() + "\n" + product.getDescription()));
    }
}