package models;

import org.hibernate.annotations.Type;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Password;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.db.jpa.Model;
import play.modules.s3blobs.S3Blob;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "_user")
public class User extends Model {
    @Email
    @Required
    public String email;
    @Required
    @Password
    public String password;
    @Required
    public String fullname;

    @Lob
    @MaxSize(130)
    @Type(type = "org.hibernate.type.TextType")
    public String about;

    public S3Blob photo;

    public boolean isAdmin;

    @ManyToMany
    public List<Category> categories;
    @ManyToMany
    public List<Product> products;

    @ManyToMany
    public List<User> friends;

    public User() {
    }

    @Override
    public String toString() {
        return fullname;
    }

    public static User connect(String email, String password) {
        return find("byEmailAndPassword", email, password).first();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        User user = (User) o;

        return id !=null && user.id != null && id.equals(user.id);

    }

    @Override
    public void _delete() {
        List<User> users = JPA.em().createQuery("select user from User as user left join user.friends as friend where friend.id = " + id).getResultList();
        for (User user : users) {
            user.friends.remove(this);
            user.save();
        }
        super._delete();
        photo.delete();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
