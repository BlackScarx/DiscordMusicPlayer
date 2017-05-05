package net.blackscarx.discordmusicplayer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.blackscarx.discordmusicplayer.object.AudioTrackView;

import javax.imageio.ImageIO;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by BlackScarx on 01-05-17. BlackScarx All right reserved
 */
public class DiscordMusicPlayer extends Application {

    public static ResourceBundle lang = ResourceBundle.getBundle("lang.di", new Locale(System.getProperty("user.language")));
    public static DiscordManager manager;
    public static DiscordMusicPlayer instance;

    public Stage stage;
    public ObservableList<AudioTrackView> list = FXCollections.observableArrayList();
    public EventHandler<WindowEvent> close = new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            System.exit(0);
        }
    };

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        stage = primaryStage;
        Parent login = FXMLLoader.load(DiscordMusicPlayer.class.getResource("/login.fxml"), lang);
        stage.setTitle("DiscordMusicPlayer");
        stage.getIcons().add(Utils.makeRoundedCorner(ImageIO.read(DiscordMusicPlayer.class.getResourceAsStream("/img/icon.png")), 15));
        stage.setResizable(false);
        stage.setOnCloseRequest(close);
        stage.setScene(new Scene(login));
        stage.show();
    }

}
