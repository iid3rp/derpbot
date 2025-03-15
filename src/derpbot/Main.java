package derpbot;

import derpbot.bot.Derpbot;
import derpbot.bot.MessageListener;
import derpbot.bot.SlashCommandExample;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        String token = Derpbot.getToken();

        if (token == null || token.isEmpty()) {
            System.err.println("environment variable not set!");
            return;
        }

        JDABuilder jdaBuilder = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .addEventListeners(new MessageListener(), new SlashCommandExample());

        JDA jda = jdaBuilder.build();

        jda.awaitReady();
        SlashCommandExample.registerCommands(jda);
    }
}