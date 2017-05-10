package net.blackscarx.discordmusicplayer.object;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.blackscarx.discordmusicplayer.Utils;

import java.io.File;

/**
 * Created by BlackScarx on 02-05-17. BlackScarx All right reserved
 */
public class AudioTrackView {

    public AudioTrack audioTrack;
    public String name;
    public String duration;

    public AudioTrackView(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
        name = !audioTrack.getInfo().title.equals("Unknown title") ? audioTrack.getInfo().title : new File(audioTrack.getIdentifier()).getName().substring(0, new File(audioTrack.getIdentifier()).getName().lastIndexOf('.'));
        duration = Utils.getFormattedTime(audioTrack.getDuration());
    }

}
