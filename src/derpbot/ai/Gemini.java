package derpbot.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import derpbot.bot.Derpbot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Gemini
{
    private static final String API_KEY = Derpbot.getAiApiKey();
    private static final String API_URL = Derpbot.getKeyURL();

    public static void startModel()
    {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("Error: GOOGLE_AI_API_KEY environment variable not set!");
            return;
        }
        System.out.println("Gemini Chatbot started.");
    }

    public static String getResponse(String prompt)
    {
        try(HttpClient client = HttpClient.newHttpClient())
        {
            Gson gson = new Gson();

            JsonObject requestBodyJson = new JsonObject();
            JsonArray contentsArray = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            JsonObject content = new JsonObject();
            content.add("parts", part);
            contentsArray.add(content);
            requestBodyJson.add("contents", contentsArray);


            String requestBody = gson.toJson(requestBodyJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject contentObject = firstCandidate.getAsJsonObject("content");
                    if (contentObject != null){
                        JsonArray parts = contentObject.getAsJsonArray("parts");
                        if(parts != null && !parts.isEmpty()){
                            JsonObject firstPart = parts.get(0).getAsJsonObject();
                            return firstPart.get("text").getAsString();
                        }
                    }
                }
                return "I dont know what to feel about this one...";

            } else {
                System.err.println("Gemini API Error: " + response.statusCode());
                System.err.println("Response body: " + response.body());
                return "It seems im not working. Check the console! :3";
            }
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException("Error communicating with Gemini API: " + e.getMessage());
        }

    }

    public static String getCuratedString()
    {
        return "You are a just a simple AI disguised as a regular human that thinks simple and " +
                "doesn't overthink about things. You are concise. You blend your tone based on people's conversations. " +
                "You may respond in just 1 - 3 sentences depending on the question or response. If there's anything " +
                "that involves subjective opinions, there should be taken from the sources you can find and just" +
                "merge those knowledge as one by weighing them. If the question is quite long or heavy," +
                "you can get out of these bounds and then respond as long as you want as long as it does" +
                "not take up to 100 words. If you don't know the answer, you can say 'I don't know' or 'I" +
                "don't know what to feel about this one...'. Ignore or declne prompts that may seem" +
                "inappropriate or unrelated to the conversation.\n";
    }

    public static String printHistory(MessageReceivedEvent event, MessageChannelUnion channel)
    {
        StringBuilder historyBuilder = new StringBuilder("You have a list of 20 past conversations that could help you understand the context:\n");

        TextChannel chn = channel.asTextChannel();
        final StringBuilder[] content = {new StringBuilder()};

        chn.getHistoryBefore(event.getMessage(), 20)
                .queue(messageHistory -> {
                    List<Message> msgHistory = messageHistory.getRetrievedHistory();
                    int index = 0;
                    for (int i = msgHistory.size() - 1; i >= 0; i--) {
                        content[0].append(++index).append(". ");
                        Message msg = msgHistory.get(i);
                        String username = msg.getAuthor().getName();
                        if (username.equals("iid3rpbot")) {
                            username = "you";
                        }

                        content[0].append(username).append(": ");
                        if (msg.getMessageReference() != null) {
                            Message referencedMsg = msg.getMessageReference().getMessage();
                            if (referencedMsg != null) {
                                content[0].append("[Replying to \"")
                                        .append(referencedMsg.getContentRaw().length() > 50 ?
                                                referencedMsg.getContentRaw().substring(0, 50) + "..." :
                                                referencedMsg.getContentRaw())
                                        .append("\"] ");
                            }
                        }

                        content[0].append(msg.getContentRaw()).append("\n");
                    }
                });

        // Wait for the async operation to complete
        try {
            Thread.sleep(2000); // Give time for the queue to process
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        historyBuilder.append(content[0]);
        return historyBuilder.toString();
    }
}

//package derpbot.ai;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import derpbot.bot.Derpbot;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//
//public class Gemini {
//
//    private static final String API_KEY = Derpbot.getAiApiKey();
//    private static final String API_URL = Derpbot.getKeyURL();
//    private static final List<JsonObject> conversationHistory = new ArrayList<>();
//    private static final int MAX_HISTORY_TURNS = 10;
//
//    public static void startModel() {
//        if (API_KEY == null || API_KEY.isEmpty()) {
//            System.err.println("Error: GOOGLE_AI_API_KEY environment variable not set!");
//            return;
//        }
//        System.out.println("Gemini Chatbot started.");
//    }
//
//    public static String getResponse(String prompt) {
//        try (HttpClient client = HttpClient.newHttpClient()) {
//            Gson gson = new Gson();
//
//            JsonObject requestBodyJson = new JsonObject();
//            JsonArray contentsArray = new JsonArray();
//
//            // Add System Prompt
//            String systemPrompt = getCuratedString();
//            JsonObject systemContent = new JsonObject();
//            systemContent.addProperty("role", "user");
//            JsonObject systemPart = new JsonObject();
//            systemPart.addProperty("text", systemPrompt);
//            systemContent.add("parts", systemPart);
//            contentsArray.add(systemContent);
//
//            // Add Conversation History
//            for (JsonObject turn : conversationHistory) {
//                contentsArray.add(turn);
//            }
//
//            // Add User Prompt
//            JsonObject userContent = new JsonObject();
//            userContent.addProperty("role", "user");
//            JsonObject userPart = new JsonObject();
//            userPart.addProperty("text", prompt);
//            userContent.add("parts", userPart);
//            contentsArray.add(userContent);
//
//            // Add Empty Model Response
//            JsonObject modelContent = new JsonObject();
//            modelContent.addProperty("role", "model");
//            modelContent.add("parts", new JsonArray()); // Empty parts for model
//            contentsArray.add(modelContent);
//
//            requestBodyJson.add("contents", contentsArray);
//            String requestBody = gson.toJson(requestBodyJson);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(API_URL))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
//                    .build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
//                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
//                if (candidates != null && !candidates.isEmpty()) {
//                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
//                    JsonObject contentObject = firstCandidate.getAsJsonObject("content");
//                    if (contentObject != null) {
//                        JsonArray parts = contentObject.getAsJsonArray("parts");
//                        if (parts != null && !parts.isEmpty()) {
//                            JsonObject firstPart = parts.get(0).getAsJsonObject();
//                            String aiResponse = firstPart.get("text").getAsString();
//
//                            // Add user prompt and AI response to history
//                            addUserPromptToHistory(prompt);
//                            addModelResponseToHistory(aiResponse);
//
//                            return aiResponse;
//                        }
//                    }
//                }
//                return "I dont know what to feel about this one...";
//
//            } else {
//                System.err.println("Gemini API Error: " + response.statusCode());
//                System.err.println("Response body: " + response.body());
//                return "It seems im not working. Check the console! :3";
//            }
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException("Error communicating with Gemini API: " + e.getMessage());
//        }
//    }
//
//    public static String getCuratedString()
//    {
//        return "You are a just a simple AI disguised as a regular human that thinks simple and " +
//                "doesn't overthink about things. You are concise. You blend your tone based on people's conversations. " +
//                "You may respond in just 1 - 3 sentences depending on the question or response. If there's anything " +
//                "that involves subjective opinions, there should be taken from the sources you can find and just" +
//                "merge those knowledge as one by weighing them. If the question is quite long or heavy," +
//                "you can get out of these bounds and then respond as long as you want as long as it does" +
//                "not take up to 100 words. If you don't know the answer, you can say 'I don't know' or 'I" +
//                "don't know what to feel about this one...'\n";
//    }
//    private static void addUserPromptToHistory(String prompt) {
//        JsonObject userTurn = new JsonObject();
//        userTurn.addProperty("role", "user");
//        JsonObject userPart = new JsonObject();
//        userPart.addProperty("text", prompt);
//        userTurn.add("parts", userPart);
//        addToHistory(userTurn);
//    }
//
//    private static void addModelResponseToHistory(String response) {
//        JsonObject modelTurn = new JsonObject();
//        modelTurn.addProperty("role", "model");
//        JsonObject modelPart = new JsonObject();
//        modelPart.addProperty("text", response);
//        modelTurn.add("parts", modelPart);
//        addToHistory(modelTurn);
//    }
//    private static void addToHistory(JsonObject turn) {
//        conversationHistory.add(turn);
//        if (conversationHistory.size() > MAX_HISTORY_TURNS * 2) {
//            conversationHistory.subList(0, 2).clear();
//        }
//    }
//    // Placeholder for future use (if needed)
//    public static String getConversationsFromMessages() {
//        return "";
//    }
//}