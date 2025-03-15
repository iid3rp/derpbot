
package derpbot.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelloWorldPing extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        String[] messages = message.split(" ");

        if (message.equalsIgnoreCase("hello world"))
            event.getChannel().sendMessage("Hello to you too!").queue();

        if(message.equalsIgnoreCase("nanotime"))
            event.getChannel().sendMessage("System's Nano time: " + System.nanoTime()).queue();

        if(message.equalsIgnoreCase("currenttime"))
            event.getChannel().sendMessage("Current time: <t:" + System.currentTimeMillis() / 1000 + ">").queue();

    }
}