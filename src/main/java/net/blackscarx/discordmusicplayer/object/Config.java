package net.blackscarx.discordmusicplayer.object;

import java.io.*;

/**
 * Created by BlackScarx on 30-04-17. BlackScarx All right reserved
 */
public class Config implements Serializable {

    public static Config config;
    private static String home = System.getProperty("user.home");
    private static File saveFile = new File(home, "DiscordMusicPlayer");
    private static File configFile = new File(saveFile, "config.dmp");

    static {
        config = new Config();
    }

    public String token = "";
    public String background = "";
    public String lang = "";
    public String botGame = "";

    public static void load() {
        if (!saveFile.exists() || !saveFile.isDirectory()) {
            saveFile.mkdir();
        }
        if (configFile.exists()) {
            try {
                FileInputStream is = new FileInputStream(configFile);
                ObjectInputStream ois = new ObjectInputStream(is);
                config = (Config) ois.readObject();
                ois.close();
                is.close();
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                e.printStackTrace();
                config = new Config();
            }
        } else {
            config = new Config();
        }
    }

    public static void save() {
        if (!saveFile.exists() || !saveFile.isDirectory()) {
            saveFile.mkdir();
        }
        if (Config.configFile == null) {
            config = new Config();
        }
        try {
            FileOutputStream os = new FileOutputStream(configFile);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(config);
            oos.flush();
            oos.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
