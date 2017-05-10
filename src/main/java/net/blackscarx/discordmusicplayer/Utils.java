package net.blackscarx.discordmusicplayer;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.StageStyle;
import net.blackscarx.discordmusicplayer.object.Playlist;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by BlackScarx on 02-05-17. BlackScarx All right reserved
 */
public class Utils {

    static javafx.scene.image.Image makeRoundedCorner(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 15, 15));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        WritableImage wr = new WritableImage(output.getWidth(), output.getHeight());
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < output.getWidth(); x++) {
            for (int y = 0; y < output.getHeight(); y++) {
                pw.setArgb(x, y, output.getRGB(x, y));
            }
        }
        return wr;
    }

    static void savePlaylist(Playlist playlist, File file) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            ObjectOutputStream object = new ObjectOutputStream(out);
            object.writeObject(playlist);
            object.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Playlist loadPlaylist(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            ObjectInputStream object = new ObjectInputStream(in);
            Object playlist = object.readObject();
            if (playlist instanceof Playlist) {
                object.close();
                in.close();
                return (Playlist) playlist;
            } else {
                object.close();
                in.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Playlist();
    }

    static ButtonType showDialog(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setContentText("");
        alert.getDialogPane().setHeader(new ImageView(new Image(message)));
        alert.showAndWait();
        return alert.getResult();
    }

    public static String getFormattedTime(Long duration) {
        Date dur = new Date(duration);
        DateFormat format = new SimpleDateFormat("H:m:ss");
        String durString = format.format(dur);
        DateFormat h = new SimpleDateFormat("H");
        if (Integer.valueOf(h.format(dur)) == 1) {
            durString = durString.replaceFirst("1:", "");
        } else {
            durString = durString.replaceFirst(h.format(dur), String.valueOf((Integer.valueOf(h.format(dur)) - 1)));
        }
        return durString;
    }

}
