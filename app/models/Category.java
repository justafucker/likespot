package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class Category extends Model {
    @Required
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
