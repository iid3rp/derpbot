package derpbot.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import derpbot.bot.Derpbot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Chatbot {

    private static final String API_KEY = Derpbot.getAiApiKey();
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash-lite:generateContent?key=" + API_KEY;

    public static void main(String[] args) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("Error: GOOGLE_AI_API_KEY environment variable not set!");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Gemini Chatbot started. Type 'exit' to quit.");

        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("Exiting...");
                break;
            }
            String response = getChatbotResponse(userInput);
            System.out.println("AI: " + response);
        }

        scanner.close();
    }

    public static String getChatbotResponse(String prompt) {
        try(HttpClient client = HttpClient.newHttpClient())
        {
            Gson gson = new Gson();

            // Create the request body as a JSON object
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
                // Parse the JSON response
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                //Safe handling incase these don't exist in the response:
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
                return "No response from AI.";

            } else {
                System.err.println("Gemini API Error: " + response.statusCode());
                System.err.println("Response body: " + response.body()); // Important for debugging
                return "Error: Could not retrieve response from Gemini.";
            }
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException("Error communicating with Gemini API: " + e.getMessage());
        }

    }
}