package BotConfigs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class MusicManager {
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.musicManagers = new HashMap<>();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild, TextChannel textChannel) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, textChannel);
            musicManagers.put(guildId, musicManager);
        } else if (textChannel != null) {
            musicManager.setNotificationChannel(textChannel);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        return getGuildAudioPlayer(guild, null);
    }

    public void loadAndPlay(Guild guild, String trackUrl, TextChannel textChannel, Consumer<String> callback) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild, textChannel);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (musicManager.player.getPlayingTrack() == null) {
                    musicManager.scheduler.play(track);
                    callback.accept("🎶 Tocando agora: **" + track.getInfo().title + "**");
                } else {
                    musicManager.scheduler.queue(track);
                    callback.accept("📥 Adicionado à fila: **" + track.getInfo().title + "**");
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    AudioTrack firstTrack = playlist.getTracks().get(0);
                    if (musicManager.player.getPlayingTrack() == null) {
                        musicManager.scheduler.play(firstTrack);
                        callback.accept("🎶 Tocando agora: **" + firstTrack.getInfo().title + "**");
                    } else {
                        musicManager.scheduler.queue(firstTrack);
                        callback.accept("📥 Adicionado à fila: **" + firstTrack.getInfo().title + "**");
                    }
                } else {
                    for (AudioTrack track : playlist.getTracks()) {
                        musicManager.scheduler.queue(track);
                    }
                    callback.accept("📚 Playlist adicionada: **" + playlist.getName() + "** com " + 
                                  playlist.getTracks().size() + " músicas");
                }
            }

            @Override
            public void noMatches() {
                callback.accept("❌ Nenhuma música encontrada para: " + trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                callback.accept("❌ Falha ao carregar: " + exception.getMessage());
            }
        });
    }

    public void skipTrack(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.scheduler.nextTrack();
        musicManager.notify("⏭️ Música pulada!");
    }

    public void pause(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.player.setPaused(true);
        musicManager.notify("⏸️ Música pausada");
    }

    public void resume(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.player.setPaused(false);
        musicManager.notify("▶️ Música retomada");
    }

    public void stop(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.scheduler.clearQueue();
        musicManager.player.stopTrack();
        musicManager.notify("⏹️ Playback parado e fila limpa");
    }

    public String getNowPlaying(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        AudioTrack track = musicManager.scheduler.getCurrentTrack();
        return track != null ? 
            String.format("🎶 **Tocando agora:** [%s](%s) `[%s]`",
                track.getInfo().title,
                track.getInfo().uri,
                formatDuration(track.getDuration())) 
            : "Nenhuma música tocando no momento";
    }

    public String getQueueStatus(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        return musicManager.scheduler.getQueueStatus();
    }

    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}