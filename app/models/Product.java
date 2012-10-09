package models;

import org.hibernate.annotations.Type;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.JPA;
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
    @MaxSize(130)
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Required
    private Date date;

    @Required
    public S3Blob photo;

    @ManyToOne
    private Product parent;

    @ManyToOne
    private Category category;

    @NoJSON
    @Required
    @ManyToOne
    private User author;

    private String url;

    private String twitter;

    private String facebook;

    private String vk;

    private String youTube;

    private String lastFM;

    private String cloudsound;

    private String store;

    private String review;

    private String foursquare;

    private Boolean draft;

    @NoJSON
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "parent")
    public List<Product> children;

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

    public String getYouTube() {
        return youTube;
    }

    public void setYouTube(String youTube) {
        this.youTube = youTube;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getCloudsound() {
        return cloudsound;
    }

    public void setCloudsound(String cloudsound) {
        this.cloudsound = cloudsound;
    }

    public String getVk() {
        return vk;
    }

    public void setVk(String vk) {
        this.vk = vk;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getFoursquare() {
        return foursquare;
    }

    public void setFoursquare(String foursquare) {
        this.foursquare = foursquare;
    }

    public String getLastFM() {
        return lastFM;
    }

    public void setLastFM(String lastFM) {
        this.lastFM = lastFM;
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

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Override
    public void _delete() {
        List<User> users = JPA.em().createQuery("select user from User as user left join user.products as product where product.id = " + id).getResultList();
        for (User user : users) {
            user.products.remove(this);
            user.save();
        }
        super._delete();
    }


    @Override
    public String toString() {
        return title;
    }
}
