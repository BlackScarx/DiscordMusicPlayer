package net.blackscarx.discordmusicplayer;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import net.blackscarx.discordmusicplayer.object.AudioPlayerSendHandler;
import net.blackscarx.discordmusicplayer.object.AudioTrackView;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by BlackScarx on 02-05-17. BlackScarx All right reserved
 */
public class DiscordManager {

    public JDA jda;
    public String guildId;
    public String channelId;
    public AudioPlayerManager remoteManager = new DefaultAudioPlayerManager();
    public AudioPlayerManager localManager = new DefaultAudioPlayerManager();
    public AudioPlayer player = remoteManager.createPlayer();
    public AudioPlayerSendHandler handler = new AudioPlayerSendHandler(player);
    public ObservableList<AudioTrackView> playList = FXCollections.observableArrayList();
    public boolean noMatch = false;

    public DiscordManager(String token) throws LoginException, InterruptedException, RateLimitedException {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(token).setBulkDeleteSplittingEnabled(false).buildBlocking();
        } catch (AccountTypeException e) {
            jda = new JDABuilder(AccountType.CLIENT).setToken(token).setBulkDeleteSplittingEnabled(false).buildBlocking();
            JOptionPane.showMessageDialog(null, DiscordMusicPlayer.lang.getString("warningClient"), DiscordMusicPlayer.lang.getString("warningClientTitle"), JOptionPane.WARNING_MESSAGE);
        }
        jda.getPresence().setGame(Game.of("powered by DiscordMusicPlayer"));
        AudioSourceManagers.registerRemoteSources(remoteManager);
        AudioSourceManagers.registerLocalSource(localManager);
        player.addListener(new Musique());
    }

    public List<Guild> getGuilds() {
        return jda.getGuilds();
    }

    public List<VoiceChannel> getChannelsGuild(String guildId) {
        return jda.getGuildById(guildId).getVoiceChannels();
    }

    public void connectGuild(String guildId) {
        if (this.guildId != null) {
            disconnectGuild();
        }
        this.guildId = guildId;
        jda.getGuildById(guildId).getAudioManager().setSendingHandler(handler);
    }

    public void disconnectChannel() {
        jda.getGuildById(guildId).getAudioManager().closeAudioConnection();
        channelId = null;
    }

    public void disconnectGuild() {
        if (channelId != null)
            disconnectChannel();
        if (guildId != null) {
            jda.getGuildById(guildId).getAudioManager().setSendingHandler(null);
            guildId = null;
        }
    }

    public void connectChannel(String channelId) throws PermissionException {
        try {
            jda.getGuildById(guildId).getAudioManager().openAudioConnection(jda.getVoiceChannelById(channelId));
            this.channelId = channelId;
        } catch (PermissionException e) {
            throw new PermissionException(e.getPermission(), e.getMessage());
        }
    }

    public void addSource(String source, boolean isRemote, boolean wait) {
        Future<Void> add = !isRemote ? localManager.loadItem(source, new AudioLoad()) : remoteManager.loadItem(source, new AudioLoad());
        if (wait) {
            try {
                add.get();
                if (noMatch) {
                    noMatch = false;
                    YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest httpRequest) throws IOException {
                        }
                    }).setApplicationName("DiscordMusicPlayer").build();
                    try {
                        YouTube.Search.List search = youtube.search().list("id,snippet");
                        search.setKey("AIzaSyBbS5X3Cf5yJilL5c2m9D3-pX72dRTLdhw");
                        search.setQ(source);
                        search.setType("video");
                        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/high/url)");
                        search.setMaxResults(10L);
                        SearchListResponse searchResponse = search.execute();
                        List<SearchResult> searchResultList = searchResponse.getItems();
                        if (searchResultList != null) {
                            for (SearchResult result : searchResultList) {
                                if (result.getId().getKind().equals("youtube#video")) {
                                    ButtonType i = Utils.showDialog(result.getSnippet().getThumbnails().getHigh().getUrl(), DiscordMusicPlayer.lang.getString("doUMean") + result.getSnippet().getTitle());
                                    if (i == ButtonType.YES) {
                                        addSource(result.getId().getVideoId(), true, true);
                                        Interface.instance.link.setText("");
                                        return;
                                    } else if (i == ButtonType.CANCEL || i == ButtonType.CLOSE) {
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    Interface.instance.link.setText("");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void play() {
        if (player.isPaused() && player.getPlayingTrack() != null) {
            player.setPaused(false);
        } else if (!playList.isEmpty() && player.getPlayingTrack() == null) {
            player.playTrack(playList.remove(0).audioTrack);
        }
    }

    public void pause() {
        if (!player.isPaused() && player.getPlayingTrack() != null) {
            player.setPaused(true);
        }
    }

    public void next() {
        if (player.getPlayingTrack() != null && !playList.isEmpty()) {
            boolean isPaused = player.isPaused();
            player.playTrack(playList.remove(0).audioTrack);
            AudioTrack audioTrack = player.getPlayingTrack();
            DiscordMusicPlayer.manager.jda.getPresence().setGame(Game.of(!audioTrack.getInfo().title.equals("Unknown title") ? audioTrack.getInfo().title : new File(audioTrack.getIdentifier()).getName().substring(0, new File(audioTrack.getIdentifier()).getName().lastIndexOf('.'))));
            player.setPaused(isPaused);
        }
    }

    public void stop() {
        if (player.getPlayingTrack() != null)
            player.stopTrack();
        if (!playList.isEmpty())
            playList.clear();
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    class AudioLoad implements AudioLoadResultHandler {

        @Override
        public void trackLoaded(AudioTrack audioTrack) {
            playList.add(new AudioTrackView(audioTrack));
        }

        @Override
        public void playlistLoaded(AudioPlaylist audioPlaylist) {
            for (AudioTrack audioTrack : audioPlaylist.getTracks()) {
                playList.add(new AudioTrackView(audioTrack));
            }
        }

        @Override
        public void noMatches() {
            noMatch = true;
        }

        @Override
        public void loadFailed(FriendlyException e) {
            e.printStackTrace();
        }

    }

    class Musique extends AudioEventAdapter {

        Timer timer;

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            if (endReason.equals(AudioTrackEndReason.FINISHED)) {
                if (!playList.isEmpty()) {
                    player.playTrack(playList.remove(0).audioTrack);
                    AudioTrack audioTrack = player.getPlayingTrack();
                    DiscordMusicPlayer.manager.jda.getPresence().setGame(Game.of(!audioTrack.getInfo().title.equals("Unknown title") ? audioTrack.getInfo().title : new File(audioTrack.getIdentifier()).getName().substring(0, new File(audioTrack.getIdentifier()).getName().lastIndexOf('.'))));
                }
            }
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            if (timer == null) {
                timer = new Timer();
                timer.start();
            }
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
            if (!playList.isEmpty()) {
                player.playTrack(playList.remove(0).audioTrack);
                AudioTrack audioTrack = player.getPlayingTrack();
                DiscordMusicPlayer.manager.jda.getPresence().setGame(Game.of(!audioTrack.getInfo().title.equals("Unknown title") ? audioTrack.getInfo().title : new File(audioTrack.getIdentifier()).getName().substring(0, new File(audioTrack.getIdentifier()).getName().lastIndexOf('.'))));
            }
        }

    }

    class Timer extends Thread implements Runnable {

        String noMusic = DiscordMusicPlayer.lang.getString("noMusic");
        String title;
        int i = 0;
        boolean isTitle = false;

        public void setTitle(AudioTrack audioTrack) {
            title = !audioTrack.getInfo().title.equals("Unknown title") ? audioTrack.getInfo().title : new File(audioTrack.getIdentifier()).getName().substring(0, new File(audioTrack.getIdentifier()).getName().lastIndexOf('.'));
        }

        @Override
        public void run() {
            while (DiscordMusicPlayer.instance.stage.isShowing()) {
                if (player.getPlayingTrack() != null) {
                    AudioTrack audioTrack = player.getPlayingTrack();
                    setTitle(audioTrack);
                    if (i == 0) {
                        DiscordMusicPlayer.manager.jda.getPresence().setGame(Game.of(title));
                        isTitle = true;
                    } else if (i == 150) {
                        DiscordMusicPlayer.manager.jda.getPresence().setGame(Game.of("powered by DiscordMusicPlayer"));
                        isTitle = false;
                    } else if (i == 200) {
                        i = -1;
                    }
                    i++;
                    Platform.runLater(() -> {
                        Interface.instance.info.setText(title);
                        Interface.instance.progress.setProgress((double) audioTrack.getPosition() / (double) audioTrack.getDuration());
                    });
                } else {
                    if (isTitle) {
                        DiscordMusicPlayer.manager.jda.getPresence().setGame(Game.of("powered by DiscordMusicPlayer"));
                        isTitle = false;
                    }
                    Platform.runLater(() -> {
                        Interface.instance.info.setText(noMusic);
                        Interface.instance.progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                    });
                }
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (isTitle) {
                DiscordMusicPlayer.manager.jda.getPresence().setGame(Game.of("powered by DiscordMusicPlayer"));
                isTitle = false;
            }
            Platform.runLater(() -> {
                Interface.instance.info.setText(noMusic);
                Interface.instance.progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            });
            this.interrupt();
        }
    }

}
