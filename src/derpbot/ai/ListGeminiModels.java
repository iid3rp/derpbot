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

public class ListGeminiModels {

    private static final String API_KEY = Derpbot.getAiApiKey();
    private static final String LIST_MODELS_URL = "https://generativelanguage.googleapis.com/v1/models?key=" + API_KEY;

    public static void main(String[] args) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("Error: GOOGLE_AI_API_KEY environment variable not set!");
            return;
        }

        try(HttpClient client = HttpClient.newHttpClient())
        {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LIST_MODELS_URL))
                    .header("Content-Type", "application/json")
                    .GET() // Use GET for ListModels
                    .build();

            HttpResponse<String> response = null;

            response = client.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                JsonArray models = jsonResponse.getAsJsonArray("models");

                if (models != null) {
                    System.out.println("Available Models:");
                    for (int i = 0; i < models.size(); i++) {
                        JsonObject model = models.get(i).getAsJsonObject();
                        String name = model.get("name").getAsString();
                        String displayName = model.get("displayName").getAsString();
                        String description = model.get("description").getAsString();
                        System.out.println("  Name: " + name);
                        System.out.println("  Display Name: " + displayName);
                        System.out.println("  Description: " + description);
                        System.out.println("  ---");
                    }
                } else {
                    System.out.println("No models found in the response.");
                }

            } else {
                System.err.println("Error listing models. Status code: " + response.statusCode());
                System.err.println("Response body: " + response.body());
            }
        }
        catch(IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}