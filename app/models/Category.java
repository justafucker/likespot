package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Category extends Model {
    @Required
    private String title;

    @ManyToOne
    private Category parent;

    @ManyToMany
    private List<User> moderators;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "parent")
    public List<Category> children;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "category")
    public List<Product> products;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<User> getModerators() {
        return moderators;
    }

    public void setModerators(List<User> moderators) {
        this.moderators = moderators;
    }

    @Override
    public String toString() {
        return title;
    }
}
