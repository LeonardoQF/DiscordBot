package BotConfigs;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Services {
    private final MusicManager musicManager;

    public Services(MusicManager musicManager) {
        this.musicManager = musicManager;
    }
    
    public List<CommandData> getCommandList() {
        List<CommandData> commands = new ArrayList<>();
        
        commands.add(Commands.slash("play", "Toca uma música do YouTube")
            .addOptions(new OptionData(OptionType.STRING, "url", "URL ou nome da música", true)));
        
        commands.add(Commands.slash("skip", "Pula a música atual"));
        commands.add(Commands.slash("pause", "Pausa a música atual"));
        commands.add(Commands.slash("resume", "Continua a música pausada"));
        commands.add(Commands.slash("hello", "Diz olá para você"));
        commands.add(Commands.slash("littlecarl", "Mostra uma imagem do Carlinhos"));
        commands.add(Commands.slash("join", "Faz o bot entrar no mesmo canal de voz em que você está"));
        
        return commands;
    }

    public void executeCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        System.out.println(commandName);
        
        try {
            switch(commandName) {
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
                case "hello":
                    executeHello(event);
                    break;
                case "littlecarl":
                	executeLittleCarl(event);
                	break;
                case "join":
                	executeJoin(event);
                	break;
                	
                default:
                    event.reply("Comando não reconhecido").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar comando " + commandName + ": " + e.getMessage());
            event.reply("Ocorreu um erro ao processar seu comando").setEphemeral(true).queue();
        }
    }

    private void executePlay(SlashCommandInteractionEvent event) {
        String url = event.getOption("url").getAsString();
        
        if (url.length() < 5) {
            event.reply("A URL/nome deve ter pelo menos 5 caracteres").setEphemeral(true).queue();
            return;
        }
        
        musicManager.loadAndPlay(event.getGuild(), url);
        event.reply("Procurando: `" + url + "`...").queue();
    }

    private void executeSkip(SlashCommandInteractionEvent event) {
        musicManager.skipTrack(event.getGuild());
        event.reply("Pulando para a próxima música...").queue();
    }

    private void executePause(SlashCommandInteractionEvent event) {
        musicManager.pause(event.getGuild());
        event.reply("Música pausada ⏸️").queue();
    }

    private void executeResume(SlashCommandInteractionEvent event) {
        musicManager.resume(event.getGuild());
        event.reply("Música retomada ▶️").queue();
    }

    private void executeHello(SlashCommandInteractionEvent event) {
        String userName = event.getUser().getName();
        event.reply("Olá, " + userName + "!").queue();
    }
    
    private void executeLittleCarl(SlashCommandInteractionEvent event) {
    	
    }
    
    private void executeJoin(SlashCommandInteractionEvent event) {
    	if (event.getMember().getVoiceState().inAudioChannel()) {
			event.reply("Você precisa estar em um canal de voz").setEphemeral(true).queue();
    		return;
		}

        AudioChannel voiceChannel = event.getMember().getVoiceState().getChannel();   
        System.out.println(voiceChannel.getName());
        event.getGuild().getAudioManager().openAudioConnection(voiceChannel); 
        event.reply("Entrei no canal: " + voiceChannel.getName()).queue();
        
        
    }
    
}