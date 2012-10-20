package models;

import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Category extends Model {
    @Required
    private String title;

    @ManyToOne
    private Category parent;

    @NoJSON
    @ManyToMany
    private List<User> moderators;

    @NoJSON
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "parent")
    public List<Category> children;

    @NoJSON
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "category")
    public List<Product> products;

    private Boolean draft;

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
    public void _delete() {
        List<User> users = JPA.em().createQuery("select user from User as user left join user.categories as category where category.id = " + id).getResultList();
        for (User user : users) {
            user.categories.remove(this);
            user.save();
        }
        super._delete();
    }

    public boolean isDraft() {
        return draft == Boolean.TRUE;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    @Override
    public String toString() {
        return title;
    }
}
