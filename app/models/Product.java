package models;

import play.data.validation.Required;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.modules.s3blobs.S3Blob;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Product extends Model {
    @Required
    private String title;

    @Required
    private String description;

    @Required
    private Date date;

    private S3Blob photo;

    @ManyToOne
    private Product parent;

    @ManyToMany
    private List<Category> categories;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public S3Blob getPhoto() {
        return photo;
    }

    public void setPhoto(S3Blob photo) {
        this.photo = photo;
    }

    public Product getParent() {
        return parent;
    }

    public void setParent(Product parent) {
        this.parent = parent;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return title;
    }
}
