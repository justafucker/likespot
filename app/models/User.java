package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class User extends Model {
    public String email;
    public String password;
    public String fullname;
    public boolean isAdmin;

    public User(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }

    @Override
    public String toString() {
        return email;
    }

    public static User connect(String email, String password) {
        return find("byEmailAndPassword", email, password).first();
    }
}
