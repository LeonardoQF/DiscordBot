package BotConfigs;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class GuildMusicManager {
    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    private TextChannel notificationChannel;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager, TextChannel channel) {
        this.player = manager.createPlayer();
        this.notificationChannel = channel;
        this.scheduler = new TrackScheduler(this.player, channel);
        this.player.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.player);
    }

    public void setNotificationChannel(TextChannel channel) {
        this.notificationChannel = channel;
        this.scheduler.setNotificationChannel(channel);
    }

    public void notify(String message) {
        if (notificationChannel != null) {
            notificationChannel.sendMessage(message).queue();
        }
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}