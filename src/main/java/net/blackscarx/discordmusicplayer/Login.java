package net.blackscarx.discordmusicplayer;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.StageStyle;
import net.blackscarx.discordmusicplayer.object.Config;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import org.controlsfx.dialog.ExceptionDialog;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by BlackScarx on 01-05-17. BlackScarx All right reserved
 */
public class Login implements Initializable {

    public TextField tokenField;
    public Button loginButton;
    public Button cancelButton;
    public Button createbot;

    public void login(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            try {
                tokenField.setDisable(true);
                DiscordMusicPlayer.manager = new DiscordManager(tokenField.getText());
                Config.config.token = tokenField.getText();
                Config.save();
                Parent interfaceParent = FXMLLoader.load(DiscordMusicPlayer.class.getResource("/interface.fxml"), DiscordMusicPlayer.lang);
                DiscordMusicPlayer.instance.stage.setScene(new Scene(interfaceParent));
            } catch (LoginException | InterruptedException | RateLimitedException e) {
                e.printStackTrace();
                if (e instanceof LoginException) {
                    Alert warn = new Alert(Alert.AlertType.WARNING, DiscordMusicPlayer.lang.getString("errorLogin"));
                    warn.initStyle(StageStyle.UTILITY);
                    warn.setTitle(DiscordMusicPlayer.lang.getString("error"));
                    warn.showAndWait();
                } else {
                    ExceptionDialog error = new ExceptionDialog(e);
                    error.initStyle(StageStyle.UTILITY);
                    error.setTitle(DiscordMusicPlayer.lang.getString("error"));
                    error.setHeaderText(DiscordMusicPlayer.lang.getString("error"));
                    error.showAndWait();
                }
                tokenField.setDisable(false);
            }
        }
    }

    public void cancel() {
        System.exit(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tokenField.setText(Config.config.token);
    }

    public void openCreatebot(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://discordapp.com/developers/applications/me/create"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
