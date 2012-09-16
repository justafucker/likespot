package models;

import play.data.validation.MaxSize;
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

    @Lob
    @Required
    @MaxSize(10000)
    private String description;

    @Required
    private Date date;

    public S3Blob photo;

    private String url;

    @ManyToOne
    private Product parent;

    @ManyToOne
    private Category category;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Product getParent() {
        return parent;
    }

    public void setParent(Product parent) {
        this.parent = parent;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return title;
    }
}
