package net.blackscarx.discordmusicplayer;

import com.google.common.base.Strings;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.blackscarx.discordmusicplayer.object.Config;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.*;

/**
 * Created by BlackScarx on 01-05-17. BlackScarx All right reserved
 */
public class DiscordMusicPlayer extends Application {

    static Map<String, ResourceBundle> langs = new HashMap<>();
    static ResourceBundle lang;
    static DiscordManager manager;
    static DiscordMusicPlayer instance;

    Stage stage;
    private EventHandler<WindowEvent> close = event -> {
        if (manager != null)
            manager.jda.shutdown();
        System.exit(0);
    };

    public static void main(String[] args) throws IOException {
        Config.load();
        for (String isoL : Locale.getISOLanguages()) {
            ResourceBundle lang;
            try {
                lang = ResourceBundle.getBundle("lang.di_" + isoL);
            } catch (MissingResourceException e) {
                continue;
            }
            langs.put(isoL, lang);
        }
        if (Strings.isNullOrEmpty(Config.config.lang)) {
            Config.config.lang = Locale.getDefault().getLanguage();
            Config.save();
        }
        if (langs.containsKey(Config.config.lang))
            lang = langs.get(Config.config.lang);
        else
            lang = langs.get("en");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        stage = primaryStage;
        Parent login = FXMLLoader.load(DiscordMusicPlayer.class.getResource("/login.fxml"), lang);
        stage.setTitle("DiscordMusicPlayer");
        stage.getIcons().add(Utils.makeRoundedCorner(ImageIO.read(DiscordMusicPlayer.class.getResourceAsStream("/img/icon.png"))));
        stage.setResizable(false);
        stage.setOnCloseRequest(close);
        stage.setScene(new Scene(login));
        stage.show();
    }

}
