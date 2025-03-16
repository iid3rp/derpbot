
package derpbot.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlashCommandExample extends ListenerAdapter {

    public static void registerCommands(JDA jda) {


        CommandListUpdateAction commands = jda.updateCommands();


        commands.addCommands(
                Commands.slash("ping", "Calculate ping of the bot")
        );


        commands.addCommands(
                Commands.slash("say", "Make the bot say something")
                        .addOptions(new OptionData(OptionType.STRING, "content", "What the bot should say")
                                .setRequired(true)) //This command requires a parameter
        );

        commands.addCommands(
                Commands.slash("roll", "Rolls an imaginary dice as random")
                        .addOptions(new OptionData(OptionType.INTEGER, "number", "Roll within a certain bound.")
                                .setRequired(true)) //This command requires a parameter
        );


        commands.addCommands(
                Commands.slash("add", "Adds two numbers together")
                        .addOptions(
                                new OptionData(OptionType.INTEGER, "num1", "The first number").setRequired(true),
                                new OptionData(OptionType.INTEGER, "num2", "The second number").setRequired(true)
                        )
        );

        commands.queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(false).queue();

        switch (event.getName()) {
            case "ping":
                long time = System.currentTimeMillis();
                event.getHook().sendMessage("Pong!").queue(m -> {
                    m.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                });
                break;
            case "say":
                event.getHook().sendMessage(Objects.requireNonNull(event.getOption("content")).getAsString()).queue();
                break;
            case "roll":
                event.getHook().sendMessage(
                        "Rolled a " + Derpbot.getRandom().nextInt(
                                Objects.requireNonNull(
                                        event.getOption("number")).getAsInt() + 1)).queue();
                break;
            case "add":
                int num1 = Objects.requireNonNull(event.getOption("num1")).getAsInt();
                int num2 = Objects.requireNonNull(event.getOption("num2")).getAsInt();
                event.getHook().sendMessage(String.valueOf(num1 + num2)).queue();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getName());
        }
    }
}