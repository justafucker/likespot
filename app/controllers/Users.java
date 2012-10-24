package controllers;

import models.User;
import play.cache.Cache;
import play.data.Upload;
import play.data.binding.Binder;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.modules.s3blobs.S3Blob;
import play.mvc.Before;
import play.mvc.Http;
import play.mvc.With;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

@CRUD.For(User.class)
@With(Secure.class)
public class Users extends CRUD {
    @Before
    public static void checkUser() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            renderArgs.put("user", user);
        }
    }

    public static void create() throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        User object = (User) constructor.newInstance();
        bindAndProcessPhoto(object, false);
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/blank.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/blank.html", type, object);
            }
        }
        object._save();
        flash.success(Messages.get("crud.created", type.modelName));
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        if (params.get("_saveAndAddAnother") != null) {
            redirect(request.controller + ".blank");
        }
        redirect(request.controller + ".show", object._key());
    }

    public static void save(String id) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        User object = (User) type.findById(id);
        notFoundIfNull(object);
        bindAndProcessPhoto(object, true);
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/show.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/show.html", type, object);
            }
        }
        object._save();
        flash.success(Messages.get("crud.saved", type.modelName));
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        redirect(request.controller + ".show", object._key());
    }

    private static void bindAndProcessPhoto(User user, boolean remove) throws IOException {
        params.checkAndParse();
        List<Upload> uploads = (List<Upload>) Http.Request.current().args.get("__UPLOADS");
        for (Upload upload : uploads) {
            if (upload.getFieldName().equals("object.photo") && upload.getSize() > 0) {
                BufferedImage image = ImageIO.read(upload.asStream());
                int size = Math.min(image.getWidth(), image.getHeight());
                BufferedImage cropped = image.getSubimage((image.getWidth() - size) / 2,
                        (image.getHeight() - size) / 2,
                        size,
                        size);
                final ByteArrayOutputStream output = new ByteArrayOutputStream() {
                    @Override
                    public synchronized byte[] toByteArray() {
                        return this.buf;
                    }
                };
                ImageIO.write(cropped, "png", output);
                try{
                    if (remove && user.photo.exists()) {
                        user.photo.delete();
                    }
                } catch (Exception e) {
                    // ignore
                }
                if (user.photo == null) {
                    user.photo = new S3Blob();
                }
                user.photo.set(new ByteArrayInputStream(output.toByteArray(), 0, output.size()),
                        "image/png");
                params.remove("object.photo");
            }
        }
        Cache.delete("user_photo_type_" + user.id);
        Cache.delete("user_photo_bytes_" + user.id);
        Binder.bind(user, "object", params.all());
    }
}
