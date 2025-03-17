package derpbot.bot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class SlashCommandExample extends ListenerAdapter
{
    private static final String API_KEY = Derpbot.getAiApiKey();
    private static final String API_URL_TEXT = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
    private static final String API_URL_VISION = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
    private static final String MODEL_NAME_TEXT = "gemini-2.0-flash";
    private static final String MODEL_NAME_VISION = "gemini-2.0-flash";
    private final Gson gson = new Gson();

    public static void registerCommands(JDA jda) {


        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("ask", "Ask a question using AI")
                        .addOptions(
                                new OptionData(OptionType.STRING, "prompt", "The main question or instruction for Gemini.", true),
                                new OptionData(OptionType.STRING, "pre_prompt", "Optional: Set a persona or context for the AI.", false),
                                new OptionData(OptionType.ATTACHMENT, "file", "Optional: An image or file to include with the prompt.", false)
                        )
        );

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
            case "ask":
                askGemini(event);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getName());
        }
    }

    public void askGemini(SlashCommandInteractionEvent event)
    {
        OptionMapping promptOption = event.getOption("prompt");
        OptionMapping prePromptOption = event.getOption("pre_prompt");
        OptionMapping fileOption = event.getOption("file");

        String prompt = promptOption != null ? promptOption.getAsString() : "";
        String prePrompt = prePromptOption != null ? prePromptOption.getAsString() : "";
        String combinedPrompt = prePrompt.isEmpty() ? prompt : prePrompt + "\n" + prompt; // Combine prompts

        try {
            String responseText;
            if (fileOption != null) {
                // Handle image input (Gemini Pro Vision)
                byte[] fileBytes = fileOption.getAsAttachment().getProxy().download().get().readAllBytes();
                String mimeType = fileOption.getAsAttachment().getContentType();
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    event.getHook().sendMessage("My `/ask` command only supports images so far! :3 Try again.").queue();
                    return;
                }
                responseText = getGeminiVisionResponse(combinedPrompt, fileBytes, mimeType);

            } else {
                responseText = getGeminiTextResponse(combinedPrompt);
            }


            if (responseText.length() > 2000) {
                String part1 = responseText.substring(0, 2000);
                event.getHook().sendMessage(part1).queue();
                String part2 = responseText.substring(2000);
                event.getHook().sendMessage(part2).queue();

            } else {
                event.getHook().sendMessage(responseText).queue();
            }

        } catch (Exception e) {
            event.getHook().sendMessage("There seems to be a problem! ```plaintext\n" + e.getMessage() +
                    "\n```\nPlease report to the developer if this appears.").queue();
        }
    }

    private String getGeminiTextResponse(String prompt) throws IOException, InterruptedException {
        String requestBody = buildTextRequestBody(prompt);
        HttpRequest request = buildHttpRequest(API_URL_TEXT, requestBody);
        return sendRequestAndGetResponse(request);
    }

    private String getGeminiVisionResponse(String prompt, byte[] fileBytes, String mimeType) throws Exception {
        String requestBody = buildVisionRequestBody(prompt, fileBytes, mimeType);
        HttpRequest request = buildHttpRequest(API_URL_VISION, requestBody);
        return sendRequestAndGetResponse(request);
    }



    private String buildTextRequestBody(String prompt) {
        JsonObject requestBodyJson = new JsonObject();
        JsonArray contentsArray = new JsonArray();

        JsonObject userContent = new JsonObject();
        JsonArray userParts = new JsonArray();
        JsonObject userTextPart = new JsonObject();
        userTextPart.addProperty("text", prompt);
        userParts.add(userTextPart);
        userContent.add("parts", userParts);
        userContent.addProperty("role", "user");
        contentsArray.add(userContent);

        requestBodyJson.add("contents", contentsArray);
        return gson.toJson(requestBodyJson);
    }

    private String buildVisionRequestBody(String prompt, byte[] fileBytes, String mimeType) {
        JsonObject requestBodyJson = new JsonObject();
        JsonArray contentsArray = new JsonArray();

        JsonObject userContent = new JsonObject();
        JsonArray userParts = new JsonArray();

        // Text part
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        userParts.add(textPart);

        // Image part
        JsonObject imagePart = new JsonObject();
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mimeType", mimeType);
        inlineData.addProperty("data", Base64.getEncoder().encodeToString(fileBytes));
        imagePart.add("inline_data", inlineData);
        userParts.add(imagePart);

        userContent.add("parts", userParts);
        userContent.addProperty("role", "user");
        contentsArray.add(userContent);

        requestBodyJson.add("contents", contentsArray);
        return gson.toJson(requestBodyJson);
    }


    private HttpRequest buildHttpRequest(String apiUrl, String requestBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
    }

    private String sendRequestAndGetResponse(HttpRequest request) throws IOException, InterruptedException {
        try(HttpClient client = HttpClient.newHttpClient())
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject contentObject = firstCandidate.getAsJsonObject("content");
                    if (contentObject != null) {
                        JsonArray parts = contentObject.getAsJsonArray("parts");
                        if (parts != null && !parts.isEmpty()) {
                            JsonObject firstPart = parts.get(0).getAsJsonObject();
                            return firstPart.get("text").getAsString();
                        }
                    }
                }
                return "No response from AI.";
            } else {
                System.err.println("Gemini API Error: " + response.statusCode());
                System.err.println("Response body: " + response.body());
                return "Error: Could not retrieve response from Gemini. Status Code: " + response.statusCode();
            }
        }

    }
}