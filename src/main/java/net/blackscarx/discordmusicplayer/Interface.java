package net.blackscarx.discordmusicplayer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import net.blackscarx.discordmusicplayer.object.AudioTrackView;
import net.blackscarx.discordmusicplayer.object.Playlist;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AccountManager;
import org.controlsfx.dialog.ExceptionDialog;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by BlackScarx on 02-05-17. BlackScarx All right reserved
 */
public class Interface implements Initializable {

    public static Interface instance;
    public TableView<AudioTrackView> playList;
    public TableColumn<AudioTrackView, Integer> id;
    public TableColumn<AudioTrackView, String> name;
    public TableColumn<AudioTrackView, String> duration;
    public ProgressBar progress;
    public Label info;
    public TextField link;
    public ComboBox<Guild> guilds;
    public ComboBox<VoiceChannel> channels;
    public Slider volume;
    private ChangeListener<Guild> changeGuild = new ChangeListener<Guild>() {
        @Override
        public void changed(ObservableValue<? extends Guild> observable, Guild oldValue, Guild newValue) {
            if (newValue == null)
                return;
            if (oldValue == null || !newValue.equals(oldValue)) {
                if (oldValue != null) {
                    if (DiscordMusicPlayer.manager.channelId != null)
                        DiscordMusicPlayer.manager.disconnectChannel();
                    if (DiscordMusicPlayer.manager.guildId != null)
                        DiscordMusicPlayer.manager.disconnectGuild();
                }
                DiscordMusicPlayer.manager.connectGuild(newValue.getId());
                channels.getItems().setAll(DiscordMusicPlayer.manager.getChannelsGuild(newValue.getId()));
            }
        }
    };
    private ChangeListener<Number> changeVolume = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            DiscordMusicPlayer.manager.setVolume(newValue.intValue());
        }
    };

    public Interface() {
        instance = this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        id.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getTableView().getItems().indexOf(param.getValue()) + 1));
        name.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().name));
        duration.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().duration));
        playList.itemsProperty().bindBidirectional(new SimpleObjectProperty<>(DiscordMusicPlayer.manager.playList));
        playList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        playList.setSortPolicy(param -> false);
        guilds.getItems().addAll(DiscordMusicPlayer.manager.getGuilds());
        guilds.setConverter(new StringConverter<Guild>() {
            @Override
            public String toString(Guild object) {
                return object.getName();
            }

            @Override
            public Guild fromString(String string) {
                return null;
            }
        });
        guilds.valueProperty().addListener(changeGuild);
        channels.setConverter(new StringConverter<VoiceChannel>() {
            @Override
            public String toString(VoiceChannel object) {
                return object.getName();
            }

            @Override
            public VoiceChannel fromString(String string) {
                return null;
            }
        });
        DiscordMusicPlayer.manager.setVolume(40);
        volume.valueProperty().addListener(changeVolume);
    }

    public void play(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            DiscordMusicPlayer.manager.play();
        }
    }

    public void fileEx(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(DiscordMusicPlayer.lang.getString("chooseFileTitle"));
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Multimedia", "*.mp3", "*.mp4", "*.flac", "*.aac", "*.mkv", "*.webm", "*.m4a", "*.ogg", "*.m3u", "*.pls"));
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            List<File> music = fileChooser.showOpenMultipleDialog(DiscordMusicPlayer.instance.stage);
            if (music != null) {
                for (File file : music) {
                    DiscordMusicPlayer.manager.addSource(file.getPath(), false, true);
                }
            }
        }
    }

    public void addLink(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            DiscordMusicPlayer.manager.addSource(link.getText(), true, true);
        }
    }

    public void pause(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            DiscordMusicPlayer.manager.pause();
        }
    }

    public void next(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            DiscordMusicPlayer.manager.next();
        }
    }

    public void connect(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (channels.getValue() != null) {
                DiscordMusicPlayer.manager.connectChannel(channels.getValue().getId());
            }
        }
    }

    public void addGuild(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://discordapp.com/api/oauth2/authorize?client_id=" + DiscordMusicPlayer.manager.jda.getSelfUser().getId() + "&scope=bot&permissions=0"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void refresh(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (DiscordMusicPlayer.manager.channelId != null)
                DiscordMusicPlayer.manager.disconnectChannel();
            if (DiscordMusicPlayer.manager.guildId != null)
                DiscordMusicPlayer.manager.disconnectGuild();
            guilds.setValue(null);
            guilds.getItems().setAll(DiscordMusicPlayer.manager.getGuilds());
            channels.getItems().clear();
        }
    }

    public void showInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(DiscordMusicPlayer.lang.getString("info"));
        alert.setContentText(DiscordMusicPlayer.lang.getString("musicName") + ": " + DiscordMusicPlayer.manager.jda.getSelfUser().getName() + "\nId: " + DiscordMusicPlayer.manager.jda.getSelfUser().getId());
        alert.showAndWait();
    }

    public void renameBot() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(DiscordMusicPlayer.lang.getString("renameBot"));
        alert.getDialogPane().setHeaderText(DiscordMusicPlayer.lang.getString("renameBotMessage"));
        TextField newName = new TextField(DiscordMusicPlayer.manager.jda.getSelfUser().getName());
        alert.getDialogPane().setContent(newName);
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.OK)) {
            if (!newName.getText().equals(DiscordMusicPlayer.manager.jda.getSelfUser().getName())) {
                try {
                    new AccountManager(DiscordMusicPlayer.manager.jda.getSelfUser()).setName(newName.getText()).complete();
                } catch (Exception e) {
                    ExceptionDialog warn = new ExceptionDialog(e);
                    warn.showAndWait();
                }
            }
        }
    }

    public void logout() throws IOException {
        if (DiscordMusicPlayer.manager.channelId != null) {
            DiscordMusicPlayer.manager.disconnectChannel();
        }
        if (DiscordMusicPlayer.manager.guildId != null) {
            DiscordMusicPlayer.manager.disconnectGuild();
        }
        DiscordMusicPlayer.manager.stop();
        DiscordMusicPlayer.manager.setVolume(40);
        playList.getItems().clear();
        volume.setValue(40);
        DiscordMusicPlayer.manager.jda.shutdown(false);
        Parent login = FXMLLoader.load(DiscordMusicPlayer.class.getResource("/login.fxml"), DiscordMusicPlayer.lang);
        DiscordMusicPlayer.instance.stage.setScene(new Scene(login));
    }

    public void loadPlaylist() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(DiscordMusicPlayer.lang.getString("loadPlaylist"));
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DiscordMusicPlayerPlaylist", "*.dmplaylist"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File music = fileChooser.showOpenDialog(DiscordMusicPlayer.instance.stage);
        if (music != null) {
            Playlist playlist = Utils.loadPlaylist(music);
            for (Playlist.Properties properties : playlist.playlist) {
                DiscordMusicPlayer.manager.addSource(properties.identifier, properties.isRemote, true);
            }
        }
    }

    public void savePlaylist() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(DiscordMusicPlayer.lang.getString("loadPlaylist"));
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DiscordMusicPlayerPlaylist", "*.dmplaylist"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File music = fileChooser.showSaveDialog(DiscordMusicPlayer.instance.stage);
        if (music != null) {
            if (!music.getName().endsWith(".dmplaylist")) {
                music = new File(music.getPath().concat(".dmplaylist"));
            }
            Utils.savePlaylist(new Playlist(DiscordMusicPlayer.manager.playList), music);
        }
    }

    public void clearPlaylist() {
        playList.getItems().clear();
    }

    public void showOption(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
            if (playList.getSelectionModel().getSelectedItems().isEmpty())
                return;
            ContextMenu menu = new ContextMenu();
            MenuItem menuItem = new MenuItem(DiscordMusicPlayer.lang.getString("deleteOption"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    playList.getItems().removeAll(playList.getSelectionModel().getSelectedItems());
                    playList.getSelectionModel().clearSelection();
                    menu.hide();
                }
            });
            menu.getItems().add(menuItem);
            menu.show(DiscordMusicPlayer.instance.stage, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        }
    }

    public void showAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle(DiscordMusicPlayer.lang.getString("about"));
        about.setContentText("Author: BlackScarx\nVersion: 3.1.0");
        about.showAndWait();
    }

    public void donate() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.paypal.me/BlackScarx"));
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void licence() {
        Alert licence = new Alert(Alert.AlertType.INFORMATION);
        licence.setTitle("Licence");
        VBox vBox = new VBox();
        Label label = new Label("Licence Creative Commons");
        Hyperlink hyperlink = new Hyperlink("Creative Commons");
        hyperlink.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://creativecommons.org/licenses/by-nc-sa/4.0/"));
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        vBox.getChildren().add(label);
        vBox.getChildren().add(hyperlink);
        licence.getDialogPane().setContent(vBox);
        licence.showAndWait();
    }
}
