
package derpbot.bot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MessagePing extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        String[] messages = message.split(" ");

        if(messages[0].equalsIgnoreCase("delimiter"))
            if(messages.length > 1)
                if(messages[1].contains(":"))
                    event.getChannel().sendMessage("Please put a delimiter without using `:`. Try again!").queue();
                else {
                    event.getChannel().sendMessage("Delimter changed to" + messages[1]).queue();
                }
            else
                event.getChannel().sendMessage("Please add a char sequence after the `delimeter` word.").queue();
        if (message.equalsIgnoreCase("hello world"))
            event.getChannel().sendMessage("Hello to you too!").queue();

        if(message.equalsIgnoreCase(Derpbot.getDelimiter() + "nanotime"))
            event.getChannel().sendMessage("System's Nano time: " + System.nanoTime()).queue();

        if(message.equalsIgnoreCase(Derpbot.getDelimiter() + "current"))
            event.getChannel().sendMessage("Current time: <t:" + System.currentTimeMillis() / 1000 + ">").queue();

        if (message.equalsIgnoreCase(Derpbot.getDelimiter() + "ping"))
        {
            long startTime = System.currentTimeMillis();
            event.getChannel().sendMessage("Pong!").queue(msg -> {
                long endTime = System.currentTimeMillis();
                long ping = endTime - startTime;
                msg.editMessage("Pong! \n-# delay: " + ping + "ms").queue(); //Edit to show the ping
            });
        }

        // rolls a dice
        if(messages[0].equalsIgnoreCase(Derpbot.getDelimiter() + "roll"))
            if(messages.length > 1)
                if(Try.parseInt(messages[1]))
                    event.getChannel().sendMessage("You rolled: " +
                            Derpbot.getRandom().nextInt(Integer.parseInt(messages[1]))).queue();

        if(message.equalsIgnoreCase(Derpbot.getDelimiter() + "lorem"))
            event.getChannel().sendMessage(
                    "Lorem ipsum odor amet, consectetuer adipiscing elit." +
                    " Tempus fusce nostra quisque nisl velit justo semper. " +
                    "Hendrerit nec tellus metus lacus turpis scelerisque. " +
                    "Ullamcorper penatibus orci a elit risus nulla. Netus taciti " +
                    "senectus egestas consectetur hac urna fringilla." +
                    " Augue pulvinar facilisi proin magna cubilia gravida senectus lacinia." +
                    " Viverra risus bibendum habitant elit porta; " +
                    "curae donec ultricies. Sodales cras nibh sem platea " +
                    "litora risus gravida. Nec a orci vulputate massa class rhoncus.").queue();

        // adds a message into the list
        if(messages[0].equalsIgnoreCase(Derpbot.getDelimiter() + "add"))
        {
            if(messages.length == 1)
                event.getChannel().sendMessage("`" + Derpbot.getDelimiter() + "add [add message here]`").queue();
            StringBuilder s = new StringBuilder();
            s.append(messages[1]);
            for(int i = 2; i < messages.length; i++)
                s.append(" ").append(messages[i]);
            Derpbot.getList().add(s.toString());
            event.getChannel().sendMessage("Message has been added to a list!\n-# Message: " + s).queue();
        }

        // gets a message into the list
        if(messages[0].equalsIgnoreCase(Derpbot.getDelimiter() + "get"))
            if(messages.length > 1)
            {
                if(Try.parseInt(messages[1]))
                    if(Integer.parseInt(messages[1]) == 0)
                        event.getChannel().sendMessage("Start at 1!").queue();
                    else event.getChannel().sendMessage(Derpbot.getList().get(Integer.parseInt(messages[1]) - 1)).queue();
            }
            else
                event.getChannel().sendMessage("`get [number]`").queue();

        // purger
        if (messages[0].equalsIgnoreCase(Derpbot.getDelimiter() + "purge")) {
            try {
                if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
                    event.getChannel().sendMessage("You need `Manage Messages` permission to use this command.").queue();
                    return;
                }
                int amount = Integer.parseInt(message.substring((Derpbot.getDelimiter() + "purge ").length()));

                if (amount < 2 || amount > 100) {
                    event.getChannel().sendMessage("You can only delete between 2-100 messages.").queue();
                    return;
                }

                TextChannel channel = event.getChannel().asTextChannel();

                channel.getHistoryBefore(event.getMessage(), amount)
                        .queue(messageHistory -> {
                            List<Message> msgHistory = messageHistory.getRetrievedHistory();
                            channel.deleteMessages(msgHistory).queue(success -> {
                                event.getChannel().sendMessage("Deleted " + amount + " messages.").queue(msg -> msg.delete().queueAfter(3, java.util.concurrent.TimeUnit.SECONDS));
                            }, error -> {
                                event.getChannel().sendMessage("Error deleting messages: " + error.getMessage()).queue();
                            });
                        });

                event.getMessage().delete().queue();

            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("`!purge [amount]`").queue();
            } catch (IllegalArgumentException e) {
                event.getChannel().sendMessage("Invalid amount. Please enter a valid number between 2 and 100").queue();
            }
        }
    }
}