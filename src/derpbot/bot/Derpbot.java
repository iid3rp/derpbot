package derpbot.bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Random;

public class Derpbot {
    private static String appId;
    private static String publicKey;
    private static String clientId;
    private static int permission;
    private static String token;
    private static String secret;
    private static String aiApiKey;
    private static String delimiter = ";";
    private static Random random;
    private static LinkedList<String> list;
    private static String aiKeyURL;
    private static String[] hi = getDefaultHi();

    static
    {
        new Random();
        list = new LinkedList<>();
        random = new Random();
        try(InputStream is = new FileInputStream(System.getProperty("user.home") + File.separator + "derpbot" + File.separator +"config.txt"))
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            appId = br.readLine().split(":")[1];
            publicKey = br.readLine().split(":")[1];
            clientId = br.readLine().split(":")[1];
            permission = Integer.parseInt(br.readLine().split(":")[1]);
            token = br.readLine().split(":")[1];
            secret = br.readLine().split(":")[1];
            aiApiKey = br.readLine().split(":")[1];
            aiKeyURL = br.readLine().split("::")[1];
            br.close();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAppId()
    {
        return appId;
    }

    public static String getPublicKey()
    {
        return publicKey;
    }

    public static String getClientId()
    {
        return clientId;
    }

    public static int getPermission()
    {
        return permission;
    }

    public static String getToken()
    {
        return token;
    }

    public static String getSecret()
    {
        return secret;
    }

    public static String getDelimiter()
    {
        return delimiter;
    }

    public static Random getRandom()
    {
        return random;
    }

    public static LinkedList<String> getList()
    {
        return list;
    }

    public static String getAiApiKey()
    {
        return aiApiKey;
    }

    public static String getKeyURL()
    {
        return aiKeyURL + aiApiKey;
    }

    public static String[] getDefaultHi()
    {
        return new String[] {
                ":3 hai", "haiiiii", "so true", "wsg", "Hello.", "hi", ":wave:",
                ":pleading_face:", "owo", ":3" // might add some...
        };
    }

    public static String getRandomHi()
    {
        return hi[random.nextInt(hi.length)];
    }
}
