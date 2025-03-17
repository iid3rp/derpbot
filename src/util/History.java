package util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture; // Use CompletableFuture

public class History
{

    private static final Logger log = LoggerFactory.getLogger(History.class);

    public static String printHistory(MessageReceivedEvent event, MessageChannelUnion channel) {
        CompletableFuture<String> future = new CompletableFuture<>();

        TextChannel textChannel = channel.asTextChannel();

        textChannel.getHistoryBefore(event.getMessage(), 20)
                .queue(messageHistory -> {
                            List<Message> msgHistory = messageHistory.getRetrievedHistory();
                            StringBuilder content = new StringBuilder("You have a list of 20 past conversations that could help you understand the context:\n");

                            int index = 0;
                            for (int i = msgHistory.size() - 1; i >= 0; i--) {
                                Message msg = msgHistory.get(i);

                                Member mem1 = msg.getMember();
                                String handleName = mem1 != null? mem1.getEffectiveName() : msg.getAuthor().getEffectiveName();
                                String handleMention = msg.getAuthor().getName(); // Get the @mention

                                content.append(++index).append(". ");
                                content.append(handleName).append(" (").append(handleMention).append("): ");

                                if (msg.getMessageReference() != null) {
                                    Message referencedMsg = msg.getMessageReference().getMessage();
                                    if (referencedMsg != null) {
                                        Member mem2 = referencedMsg.getMember();
                                        String refHandleName = mem2 != null? mem2.getEffectiveName() : referencedMsg.getAuthor().getEffectiveName();
                                        String refHandleMention = referencedMsg.getAuthor().getName(); // Get @mention

                                        content.append("[Replying to ")
                                                .append(refHandleName).append(" (").append(refHandleMention).append("): \"")
                                                .append(referencedMsg.getContentRaw().length() > 50 ?
                                                        referencedMsg.getContentRaw().substring(0, 50) + "..." :
                                                        referencedMsg.getContentRaw())
                                                .append("\"] ");
                                    }
                                }
                                content.append(msg.getContentRaw()).append("\n");
                            }
                            future.complete(content.toString()); // Complete the future with the result
                        },
                        future::completeExceptionally);

        try {
            return future.get(); // Get the result (blocking, but within a CompletableFuture)
        } catch (Exception e) {
            // Handle exceptions from future.get() (InterruptedException, ExecutionException)
            log.error("e: ", e);  // Log the error
            return "Error retrieving message history: " + e.getMessage();
        }
    }

    //A more efficient way to get the history string for a particular message
    public static CompletableFuture<String> getHistoryString(Message message, int limit) {
        CompletableFuture<String> future = new CompletableFuture<>();

        message.getChannel().getHistoryBefore(message, limit)
                .queue(messageHistory -> {
                    List<Message> msgHistory = messageHistory.getRetrievedHistory();
                    StringBuilder content = new StringBuilder();

                    int index = 0;
                    for (int i = msgHistory.size() - 1; i >= 0; i--) {
                        Message msg = msgHistory.get(i);
                        String handleName = msg.getAuthor().getName();
                        String handleMention = msg.getAuthor().getAsMention();

                        content.append(++index).append(". ");
                        content.append(handleName).append(" (").append(handleMention).append("): ");

                        if (msg.getMessageReference() != null && msg.getMessageReference().getMessage() != null) {
                            Message referencedMsg = msg.getMessageReference().getMessage();
                            String refHandleName = referencedMsg.getAuthor().getName();
                            String refHandleMention = referencedMsg.getAuthor().getAsMention();

                            content.append("[Replying to ")
                                    .append(refHandleName).append(" (").append(refHandleMention).append("): \"")
                                    .append(referencedMsg.getContentRaw().length() > 50 ?
                                            referencedMsg.getContentRaw().substring(0, 50) + "..." :
                                            referencedMsg.getContentRaw())
                                    .append("\"] ");
                        }
                        content.append(msg.getContentRaw()).append("\n");
                    }
                    future.complete(content.toString());
                }, future::completeExceptionally);

        return future;
    }

    //Example usage
//    public static void main(String[] args) {
//            //Dummy event for example
//            MessageReceivedEvent event = ...;
//            MessageChannelUnion channel = event.getChannel();
//            String history = printHistory(event, channel);
//            System.out.print(history);
//    }
}