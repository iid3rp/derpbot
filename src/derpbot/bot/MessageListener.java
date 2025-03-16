
package derpbot.bot;

import derpbot.ai.Gemini;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Objects;

public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        String[] messages = message.split(" ");
        String cmdDelimiter = message.substring(0, Derpbot.getDelimiter().length());

        String guildId = event.getGuild().getId();
        MessageChannelUnion channel = event.getChannel();

        if(cmdDelimiter.equalsIgnoreCase(Derpbot.getDelimiter()))
        {
            String cmd = message.substring(cmdDelimiter.length()).split(" ")[0];

            if(cmd.equalsIgnoreCase("ping"))
                messagePinger(channel);
            
            if(cmd.equalsIgnoreCase("nanotime"))
                getNanoTime(channel);

            if(cmd.equalsIgnoreCase("current"))
                getCurrentTime(channel);

            if(cmd.equalsIgnoreCase("roll"))
                rollNumber(channel, messages);

            if(cmd.equalsIgnoreCase("loremipsum"))
                loremIpsumGenerate(channel);

            if(cmd.equalsIgnoreCase("add"))
                addMessageToList(channel, messages);

            if(cmd.equalsIgnoreCase("get"))
                getMessageFromList(channel, messages);

            if(cmd.equalsIgnoreCase("purge"))
                messagePurge(event, channel, message);
        }

        if(messages[0].equalsIgnoreCase("<@" + Derpbot.getAppId() + ">"))
            getAiResponse(event, channel, message);

        // debug
        System.out.println(Objects.requireNonNull(event.getMember()).getUser().getName() + ": " + message);
    }

    private void getAiResponse(MessageReceivedEvent event, MessageChannelUnion channel, String message)
    {
        // Extract the actual query by removing all bot mentions
        String botMention = "<@" + Derpbot.getAppId() + ">";
        String cleanedMessage = message.replace(botMention, "").trim();

        if (cleanedMessage.isEmpty()) {
            channel.sendMessage(Derpbot.getRandomHi()).setMessageReference(event.getMessage()).queue();
            return;
        }

        String response = Gemini.getResponse(Gemini.getCuratedString() + "\nCurrent prompt: " + cleanedMessage);

        channel.sendMessage(response)
                .setMessageReference(event.getMessage())
                .queue();
    }

    private void getMessageFromList(MessageChannelUnion channel, String[] messages)
    {
        if(messages.length > 1)
        {
            if(Try.parseInt(messages[1]))
                if(Integer.parseInt(messages[1]) == 0)
                    channel.sendMessage("Start at 1!").queue();
                else channel.sendMessage(Derpbot.getList().get(Integer.parseInt(messages[1]) - 1)).queue();
        }
        else
            channel.sendMessage("`get [number]`").queue();
    }

    private void addMessageToList(MessageChannelUnion channel, String[] messages)
    {
        if(messages.length == 1)
            channel.sendMessage("`" + Derpbot.getDelimiter() + "add [add message here]`").queue();
        StringBuilder s = new StringBuilder();
        s.append(messages[1]);
        for(int i = 2; i < messages.length; i++)
            s.append(" ").append(messages[i]);
        Derpbot.getList().add(s.toString());
        channel.sendMessage("Message has been added to a list!\n-# Message: " + s).queue();
    }

    private void loremIpsumGenerate(MessageChannelUnion channel)
    {
        channel.sendMessage(
                "Lorem ipsum odor amet, consectetuer adipiscing elit." +
                        " Tempus fusce nostra quisque nisl velit justo semper. " +
                        "Hendrerit nec tellus metus lacus turpis scelerisque. " +
                        "Ullamcorper penatibus orci a elit risus nulla. Netus taciti " +
                        "senectus egestas consectetur hac urna fringilla." +
                        " Augue pulvinar facilisi proin magna cubilia gravida senectus lacinia." +
                        " Viverra risus bibendum habitant elit porta; " +
                        "curae donec ultricies. Sodales cras nibh sem platea " +
                        "litora risus gravida. Nec a orci vulputate massa class rhoncus.").queue();
    }

    private void rollNumber(MessageChannelUnion channel, String[] messages)
    {
        if(messages.length == 1)
            channel.sendMessage("Rolled a **#" +
                    Derpbot.getRandom().nextInt(101) +
                    "**.\n-# Rolling dice defaults between 0-100.").queue();
        else
            if(Try.parseInt(messages[1]))
                channel.sendMessage("Rolled a **#" +
                        Derpbot.getRandom().nextInt(Integer.parseInt(messages[1])) +
                        "**.").queue();
            else channel.sendMessage("Rolled a **#" +
                    Derpbot.getRandom().nextInt(101) +
                    "**.\n-# Rolling dice defaults between 0-100.").queue();
        System.out.println(messages.length);
    }

    private void getCurrentTime(MessageChannelUnion channel)
    {
        channel.sendMessage("Current Time: " + System.currentTimeMillis() / 1000).queue();
    }

    private void getNanoTime(MessageChannelUnion channel)
    {
        channel.sendMessage("System's nano time: " + System.nanoTime()).queue();
    }

    private void messagePinger(MessageChannelUnion channel)
    {
        long startTime = System.currentTimeMillis();
        channel.sendMessage("Pong!").queue(msg -> {
            long endTime = System.currentTimeMillis();
            long ping = endTime - startTime;
            msg.editMessage("Pong! \n-# delay: " + ping + "ms").queue();
        });
    }

    private static void messagePurge(MessageReceivedEvent event, MessageChannelUnion channel, String message)
    {
        try {
            if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
                channel.sendMessage("You need `Manage Messages` permission to use this command.").queue();
                return;
            }
            if(!Try.parseInt(message.split(" ")[1]))
            {
                channel.sendMessage("`" + Derpbot.getDelimiter() + "purge [amount]`").queue();
            }

            int amount = Integer.parseInt(message.split(" ")[1]);
            if (amount < 2 || amount > 1000) {
                channel.sendMessage("You can only delete between 2-1000 messages.").queue();
                return;
            }

            TextChannel chn = channel.asTextChannel();
            chn.getHistoryBefore(event.getMessage(), amount)
                .queue(messageHistory -> {
                    List<Message> msgHistory = messageHistory.getRetrievedHistory();
                    chn.deleteMessages(msgHistory).queue(success ->
                        chn.sendMessage("Deleted " + amount + " messages.")
                            .queue(msg ->
                                msg.delete()
                                    .queueAfter(3, java.util.concurrent.TimeUnit.SECONDS)),
          error ->
                                    chn.sendMessage("Error deleting messages: " + error.getMessage()).queue());
                });
            event.getMessage().delete().queue();

        } catch (NumberFormatException e) {
            channel.sendMessage("`!purge [amount]`").queue();
        } catch (IllegalArgumentException e) {
            channel.sendMessage("Invalid amount. Please enter a valid number between 2 and 100").queue();
        }
    }
}