import com.amazonaws.services.s3.model.AmazonS3Exception;
import controllers.Products;
import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
        /*List<Product> products = Product.all().fetch();
        for (Product product : products) {
            try {
                if (!product.hasThumbnail() && product.hasPhoto() && product.getPhoto().exists()) {
                    BufferedImage original = ImageIO.read(product.getPhoto().get());
                    int size = Math.min(original.getWidth(), original.getHeight());
                    BufferedImage cropped = original.getSubimage((original.getWidth() - size) / 2,
                            (original.getHeight() - size) / 2, size, size);
                    Image thumbnailImage = cropped.getScaledInstance(Products.THUMBNAIL_SIZE, Products.THUMBNAIL_SIZE, Image.SCALE_SMOOTH);
                    BufferedImage thumbnail = new BufferedImage(Products.THUMBNAIL_SIZE, Products.THUMBNAIL_SIZE, BufferedImage.TYPE_INT_RGB);
                    Graphics graphics = thumbnail.createGraphics();
                    graphics.drawImage(thumbnailImage, 0, 0, new Color(0, 0, 0), null);
                    graphics.dispose();
                    final ByteArrayOutputStream thumbnailOutput = Products.getImageAsStream(thumbnail);
                    product.getThumbnail().set(new ByteArrayInputStream(thumbnailOutput.toByteArray(), 0, thumbnailOutput.size()),
                            "image/png");
                    product.save();
                    Logger.debug("Product #" + product.getId() + " updated.");
                }
            } catch (IOException e) {
                Logger.error("Error while updating thumbnail", e);
            } catch (AmazonS3Exception e) {
                Logger.error("Error while updating thumbnail", e);
            }
        }*/
    }
}