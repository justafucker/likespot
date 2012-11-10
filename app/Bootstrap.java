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

    }
}