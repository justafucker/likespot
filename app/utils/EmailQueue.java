package utils;

import models.Product;
import models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Davydov
 */
public class EmailQueue {
    private static volatile EmailQueue instance;

    public static EmailQueue getInstance() {
        EmailQueue singleton = instance;
        if (singleton == null) {
            synchronized (EmailQueue.class) {
                singleton = instance;
                if (singleton == null)
                    instance = singleton = new EmailQueue();
            }
        }
        return singleton;
    }

    private volatile List<Pair<User, Product>> queue;

    public EmailQueue() {
        queue = new ArrayList<Pair<User, Product>>();
    }

    public synchronized void add(User user, Product product) {
        queue.add(new Pair<User, Product>(user, product));
    }

    public synchronized List<Pair<User, Product>> getAndClear() {
        List<Pair<User, Product>> empty = new ArrayList<Pair<User, Product>>();
        List<Pair<User, Product>> current = queue;
        queue = empty;
        return current;
    }
}
