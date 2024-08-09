package net.blackscarx.discordmusicplayer.object;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.*;

/**
 * Created by BlackScarx on 30-04-17. BlackScarx All right reserved
 */
public class Config {

    public static Config config;
    private static final String home = System.getProperty("user.home");
    private static final File saveFile = new File(home, "DiscordMusicPlayer");
    private static final File configFile = new File(saveFile, "config.json");

    static {
        config = new Config();
    }

    private Config() {}

    public String token = "";
    public String background = "";
    public String lang = "";
    public String botGame = "";
    public String googleApiKey = "";

    public static void load() {
        if (!saveFile.exists() || !saveFile.isDirectory()) {
            saveFile.mkdir();
        }
        if (configFile.exists()) {
            try {
                ObjectReader reader = new ObjectMapper().reader();
                config = reader.readValue(configFile, Config.class);
            } catch (IOException | ClassCastException e) {
                e.printStackTrace();
                config = new Config();
            }
        }
    }

    public void save() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        if (!saveFile.exists() || !saveFile.isDirectory()) {
            saveFile.mkdir();
        }
        if (Config.configFile == null) {
            config = new Config();
        }
        try {
            String configValue = ow.writeValueAsString(this);
            FileOutputStream os = new FileOutputStream(configFile);
            os.write(configValue.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
