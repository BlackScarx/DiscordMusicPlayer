package net.blackscarx.discordmusicplayer;

import com.google.common.base.Strings;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.blackscarx.discordmusicplayer.object.Config;
import net.dv8tion.jda.api.entities.Activity;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.ResourceBundle;

/**
 * Created by BlackScarx on 11-05-17. BlackScarx All right reserved
 */
public class Settings implements Initializable {

    static Stage settings;
    public ComboBox<ResourceBundle> langs;
    public AnchorPane main;
    public TextField background;
    public TextField botGame;
    private String backgroundString = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (ResourceBundle lang : DiscordMusicPlayer.langs.values()) {
            langs.getItems().add(lang);
        }
        langs.setConverter(new StringConverter<ResourceBundle>() {
            @Override
            public String toString(ResourceBundle object) {
                return object.getString("name");
            }

            @Override
            public ResourceBundle fromString(String string) {
                return null;
            }
        });
        langs.setValue(DiscordMusicPlayer.lang);
        botGame.setText(Config.config.botGame);
    }

    public void close(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            settings.close();
        }
    }

    public void apply(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            DiscordMusicPlayer.lang = langs.getValue();
            StringBuilder builder = new StringBuilder(langs.getValue().getBaseBundleName());
            Config.config.lang = builder.substring(builder.length() - 2);
            Config.config.botGame = botGame.getText();
            if (!botGame.getText().equals(""))
                DiscordMusicPlayer.manager.jda.getPresence().setActivity(Activity.playing(botGame.getText()));
            if (backgroundString != null) {
                Config.config.background = backgroundString;
                Interface.instance.mainPane.setBackground(Utils.getBackground(Utils.stringToImage(backgroundString)));
            } else if (!Strings.isNullOrEmpty(background.getText())) {
                try {
                    URL back = new URL(background.getText());
                    URLConnection urlConnection = back.openConnection();
                    urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
                    InputStream in = urlConnection.getInputStream();
                    byte[] imgB = Utils.inputStreamToByteArray(in);
                    backgroundString = Base64.getEncoder().encodeToString(imgB);
                    Config.config.background = backgroundString;
                    Interface.instance.mainPane.setBackground(Utils.getBackground(Utils.stringToImage(backgroundString)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Config.save();
            settings.getScene().setRoot(FXMLLoader.load(DiscordMusicPlayer.class.getResource("/settings.fxml"), DiscordMusicPlayer.lang));
            double volume = Interface.instance.volume.getValue();
            DiscordMusicPlayer.instance.stage.getScene().setRoot(FXMLLoader.load(DiscordMusicPlayer.class.getResource("/interface.fxml"), DiscordMusicPlayer.lang));
            if (DiscordMusicPlayer.manager.isPlaying()) {
                Interface.instance.playPause.setImage(new Image("/img/pause.png"));
            }
            Interface.instance.volume.setValue(volume);
            Interface.instance.guilds.getItems().setAll(DiscordMusicPlayer.manager.getGuilds());
            if (DiscordMusicPlayer.manager.guildId != null) {
                Interface.instance.guilds.valueProperty().removeListener(Interface.instance.changeGuild);
                Interface.instance.guilds.setValue(DiscordMusicPlayer.manager.jda.getGuildById(DiscordMusicPlayer.manager.guildId));
                Interface.instance.guilds.valueProperty().addListener(Interface.instance.changeGuild);
                Interface.instance.channels.getItems().setAll(DiscordMusicPlayer.manager.getChannelsGuild(DiscordMusicPlayer.manager.guildId));
            }
            if (DiscordMusicPlayer.manager.channelId != null) {
                Interface.instance.channels.setValue(DiscordMusicPlayer.manager.jda.getVoiceChannelById(DiscordMusicPlayer.manager.channelId));
            }
        }
    }

    public void dragOver(DragEvent dragEvent) throws FileNotFoundException {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
            File image = db.getFiles().get(0);
            String mimetype = new MimetypesFileTypeMap().getContentType(image);
            String type = mimetype.split("/")[0];
            if (type.equals("image") || mimetype.equals("application/octet-stream")) {
                dragEvent.acceptTransferModes(TransferMode.LINK);
            }
        } else if (db.hasUrl()) {
            if (db.getUrl().matches(".*\\.(jpeg|jpg|gif|png)$")) {
                dragEvent.acceptTransferModes(TransferMode.LINK);
            }
        }
    }

    public void getBackground(DragEvent dragEvent) throws IOException {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
            backgroundString = Utils.imageToString(db.getFiles().get(0));
            dragEvent.setDropCompleted(true);
            dragEvent.consume();
        } else if (db.hasUrl()) {
            try {
                URL back = new URL(db.getUrl());
                URLConnection urlConnection = back.openConnection();
                urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
                InputStream in = urlConnection.getInputStream();
                byte[] imgB = Utils.inputStreamToByteArray(in);
                backgroundString = Base64.getEncoder().encodeToString(imgB);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
