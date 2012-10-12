package models;

import controllers.CRUD;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name="_user")
public class User extends Model {
    public String email;
    public String password;
    public String fullname;
    public boolean isAdmin;

    @ManyToMany
    public List<Category> categories;
    @CRUD.Hidden
    @ManyToMany
    public List<Product> products;

    public User() {
    }

    @Override
    public String toString() {
        return fullname;
    }

    public static User connect(String email, String password) {
        return find("byEmailAndPassword", email, password).first();
    }
}
