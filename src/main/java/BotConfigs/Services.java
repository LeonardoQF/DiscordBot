package BotConfigs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

public class Services {
	private final MusicManager musicManager;

	public Services(MusicManager musicManager) {
		this.musicManager = musicManager;
	}

	public List<CommandData> getCommandList() {
		List<CommandData> commands = new ArrayList<>();

		commands.add(Commands.slash("play", "Toca uma música do YouTube/Spotify")
				.addOptions(new OptionData(OptionType.STRING, "url", "URL ou nome da música", true)));
		commands.add(Commands.slash("skip", "Pula a música atual"));
		commands.add(Commands.slash("pause", "Pausa a música atual"));
		commands.add(Commands.slash("resume", "Continua a música pausada"));
		commands.add(Commands.slash("stop", "Para a música e limpa a fila"));
		commands.add(Commands.slash("queue", "Mostra a fila de músicas"));
		commands.add(Commands.slash("nowplaying", "Mostra a música atual"));
		commands.add(Commands.slash("join", "Faz o bot entrar no seu canal de voz"));
		commands.add(Commands.slash("leave", "Faz o bot sair do canal de voz"));
		commands.add(Commands.slash("hello", "Diz olá para você"));
		commands.add(Commands.slash("littlecarl", "Mostra uma imagem do Carlinhos"));
		commands.add(Commands.slash("userinfo", "Mostra informações do usuário marcado").addOption(OptionType.USER,
				"user", "Usuário que terá suas informações mostradas"));

		return commands;
	}

	public void executeCommand(SlashCommandInteractionEvent event) {
		String commandName = event.getName();

		try {
			switch (commandName) {
			case "play":
				executePlay(event);
				break;
			case "skip":
				executeSkip(event);
				break;
			case "pause":
				executePause(event);
				break;
			case "resume":
				executeResume(event);
				break;
			case "stop":
				executeStop(event);
				break;
			case "queue":
				executeQueue(event);
				break;
			case "nowplaying":
				executeNowPlaying(event);
				break;
			case "join":
				executeJoin(event);
				break;
			case "leave":
				executeLeave(event);
				break;
			case "hello":
				executeHello(event);
				break;
			case "littlecarl":
				executeLittleCarl(event);
				break;
			default:
				event.reply("Comando não reconhecido").setEphemeral(true).queue();
			}
		} catch (Exception e) {
			System.err.println("Erro ao executar comando " + commandName + ": " + e.getMessage());
			event.reply("❌ Ocorreu um erro: " + e.getMessage()).setEphemeral(true).queue();
		}
	}

	private void executePlay(SlashCommandInteractionEvent event) {
		String url = event.getOption("url").getAsString();

		if (url.length() < 3) {
			event.reply("🔍 A busca deve ter pelo menos 3 caracteres").setEphemeral(true).queue();
			return;
		}

		TextChannel textChannel = event.getChannel().asTextChannel();
		event.deferReply().queue(hook -> {
			musicManager.loadAndPlay(event.getGuild(), url, textChannel, message -> {
				hook.editOriginal(message)
						.setActionRow(Button.primary("skip", "⏭️ Pular"), Button.secondary("pause", "⏸️ Pausar"),
								Button.success("resume", "▶️ Retomar"), Button.danger("stop", "⏹️ Parar"))
						.queue();
			});
		});
	}

	private void executeSkip(SlashCommandInteractionEvent event) {
		musicManager.skipTrack(event.getGuild());
		event.reply("⏭️ Pulando para a próxima música...").queue();
	}

	private void executePause(SlashCommandInteractionEvent event) {
		musicManager.pause(event.getGuild());
		event.reply("⏸️ Música pausada").queue();
	}

	private void executeResume(SlashCommandInteractionEvent event) {
		musicManager.resume(event.getGuild());
		event.reply("▶️ Música retomada").queue();
	}

	private void executeStop(SlashCommandInteractionEvent event) {
		musicManager.stop(event.getGuild());
		event.reply("⏹️ Playback parado e fila limpa").queue();
	}

	private void executeQueue(SlashCommandInteractionEvent event) {
		String queueStatus = musicManager.getQueueStatus(event.getGuild());
		event.reply(queueStatus).queue();
	}

	private void executeNowPlaying(SlashCommandInteractionEvent event) {
		String nowPlaying = musicManager.getNowPlaying(event.getGuild());
		event.reply(nowPlaying).queue();
	}

	private void executeJoin(SlashCommandInteractionEvent event) {
		if (!event.getMember().getVoiceState().inAudioChannel()) {
			event.reply("❌ Você precisa estar em um canal de voz!").setEphemeral(true).queue();
			return;
		}

		AudioChannel voiceChannel = event.getMember().getVoiceState().getChannel();
		event.getGuild().getAudioManager().openAudioConnection(voiceChannel);
		event.reply("🔊 Conectado ao canal: " + voiceChannel.getAsMention()).queue();
	}

	private void executeLeave(SlashCommandInteractionEvent event) {
		if (!event.getGuild().getAudioManager().isConnected()) {
			event.reply("❌ Não estou em nenhum canal de voz!").setEphemeral(true).queue();
			return;
		}

		AudioChannel channel = event.getGuild().getAudioManager().getConnectedChannel();
		event.getGuild().getAudioManager().closeAudioConnection();
		event.reply("🚪 Saindo do canal: " + channel.getAsMention()).queue();
	}

	private void executeHello(SlashCommandInteractionEvent event) {
		String userName = event.getUser().getName();
		event.reply("👋 Olá, " + userName + "!").queue();
	}

	private void executeLittleCarl(SlashCommandInteractionEvent event) {
		try {
			File imageFile = new File("C:\\Users\\joaop\\Downloads\\DiscordBot\\src\\main\\resources\\carlinhos.webp");
			if (imageFile.exists()) {
				event.reply("**Dalva sua vagabunda!**").addFiles(FileUpload.fromData(imageFile)).queue();
			} else {
				event.reply("A imagem não foi encontrada!").setEphemeral(true).queue();
			}
		} catch (Exception e) {
			event.reply("Ocorreu um erro ao carregar a imagem: " + e.getMessage()).setEphemeral(true).queue();
		}
	}

	public void executeUserInfo(SlashCommandInteractionEvent event) {
		try {
			
			User user = event.getOption("user").getAsUser();
			
			String isBot = user.isBot() ? "Sim" : "Não";

			String serverUsername = user.getName();

			String globalUsername = user.getName();

			File pfp = File.createTempFile("avatar", ".png");

			FileUtils.copyURLToFile(URL.of(URI.create(user.getAvatarUrl()), null), pfp);
			String flags = user.getFlags().isEmpty() ? "Nenhuma"
					: user.getFlags().toString().replace("[", "").replace("]", "").trim();

			LocalDateTime dateTimeCriado = user.getTimeCreated().toLocalDateTime();
			
			int creationDay = dateTimeCriado.getDayOfMonth();
			int creationMonth = dateTimeCriado.getMonthValue();
			int creationYear = dateTimeCriado.getYear();
			int creationHour = dateTimeCriado.getHour();
			int creationMinutes = dateTimeCriado.getMinute();
			
			String accountCreatedDate = creationDay + "/" + creationMonth + "/" + creationYear + "às " + creationHour + ":" + creationMinutes;
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("Nome no servidor: " + serverUsername);
			sb.append("\n");
			sb.append("Nome global: " + globalUsername);
			sb.append("\n");
			sb.append("Bot: " + isBot);
			sb.append("\n");
			sb.append("Flags: " + flags);
			sb.append("\n");
			sb.append("Conta criada em: " + accountCreatedDate);
			
			event.reply(sb.toString()).addFiles(FileUpload.fromData(pfp));

		} catch (MalformedURLException e) {
			return;
		} catch (IOException e) {
			return;
		}
	}
}