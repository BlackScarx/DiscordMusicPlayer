package net.blackscarx.discordmusicplayer.object;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlackScarx on 02-02-17. BlackScarx All right reserved
 */
public class Playlist implements Serializable {

    public List<Properties> playlist = new ArrayList<>();

    public Playlist() {
    }

    public Playlist(ObservableList<AudioTrackView> audioTracks) {
        for (AudioTrackView track : audioTracks) {
            Boolean isRemote = null;
            AudioSourceManager manager = track.audioTrack.getSourceManager();
            if (manager instanceof YoutubeAudioSourceManager || manager instanceof VimeoAudioSourceManager || manager instanceof SoundCloudAudioSourceManager || manager instanceof HttpAudioSourceManager || manager instanceof BandcampAudioSourceManager)
                isRemote = true;
            else if (manager instanceof LocalAudioSourceManager)
                isRemote = false;
            if (isRemote != null)
                playlist.add(new Properties(isRemote, track.audioTrack.getIdentifier()));
        }
    }

    public class Properties implements Serializable {

        public Boolean isRemote;
        public String identifier;

        Properties(Boolean isRemote, String identifier) {
            this.isRemote = isRemote;
            this.identifier = identifier;
        }

    }

}
