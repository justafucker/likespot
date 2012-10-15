package jobs;

import models.Product;
import models.User;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.Mail;
import utils.EmailQueue;
import utils.Pair;

import java.util.*;

/**
 * @author Denis Davydov
 */
@Every("1h")
public class EmailJob extends Job {

    @Override
    public void doJob() throws Exception {
        List<Pair<User, Product>> events = EmailQueue.getInstance().getAndClear();
        Map<User, List<Product>> aggregationMap = new HashMap<User, List<Product>>();

        for (Pair<User, Product> pair : events) {
            List<Product> changedProductsForUser = aggregationMap.get(pair.getFirst());
            if (changedProductsForUser == null)
                aggregationMap.put(pair.getFirst(), changedProductsForUser = new ArrayList<Product>());
            changedProductsForUser.add(pair.getSecond());
        }

        for (Map.Entry<User, List<Product>> entry : aggregationMap.entrySet())
            processUser(entry.getKey(), entry.getValue());
    }

    private void processUser(User key, List<Product> value) {
        Set<Long> visitedProducts = new HashSet<Long>();
        List<Product> filteredProducts = new ArrayList<Product>();
        // reverse order => we get the last change of each product
        for (int i = value.size() - 1; i > -1; i--) {
            Product product = value.get(i);
            if (visitedProducts.contains(product.getId()))
                continue;
            filteredProducts.add(product);
            visitedProducts.add(product.getId());
        }

        sendHtmlEmail(key, filteredProducts);
    }

    private void sendHtmlEmail(User key, List<Product> filteredProducts) {
        try {
            final HtmlEmail email = new HtmlEmail();
            email.addTo(key.email);
            email.setFrom("noreply@likespot.ru", "Likespot");
            email.setSubject(filteredProducts.size() + " products have been changed");
            email.setCharset("UTF-8");

            email.setHtmlMsg(createHtmlMsg(filteredProducts));
            email.setTextMsg(createPlainTextMsg(filteredProducts));

            Mail.send(email);
        } catch (EmailException e) {
            Logger.error(e, e.getMessage());
        }
    }

    private String createPlainTextMsg(List<Product> filteredProducts) {
        StringBuilder builder = new StringBuilder();
        for (Product product : filteredProducts)
            builder.append(product.getTitle()).append("\n").append(product.getDescription()).append("\n");
        return builder.toString();
    }

    private String createHtmlMsg(List<Product> filteredProducts) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html>");

        for (Product product : filteredProducts) {
            builder.append("<p>").append("<b>").
                    append(product.getTitle()).append("</b>").append("<br>").
                    append(product.getDescription()).append("</p>");
        }

        return builder.append("</html>").toString();
    }
}
