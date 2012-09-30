package models;

import org.hibernate.annotations.Type;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.s3blobs.S3Blob;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Product extends Model {
    @Required
    private String title;

    @Lob
    @Required
    @MaxSize(10000)
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

    private String url;

    private String twitter;

    private String facebook;

    private String vk;

    private String youTube;

    private String lastFM;

    private String cloudsound;

    private String afisha;

    private Boolean draft;

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

    public String getAfisha() {
        return afisha;
    }

    public void setAfisha(String afisha) {
        this.afisha = afisha;
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

    @Override
    public String toString() {
        return title;
    }
}
