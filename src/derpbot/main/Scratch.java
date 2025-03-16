package derpbot.main;

import derpbot.bot.Derpbot;

import java.io.File;

public class Scratch
{
    public static void main(String[] args)
    {
        System.out.println(Derpbot.getToken());
        String s = "dghjsdfh";
        System.out.println(s.split(" ")[0]);
        String s2 = ";";
        System.out.println(s.substring(0, 1));
        File file = new File(System.getProperty("user.home") + File.separator + "config.txt");
        System.out.println(file.getAbsolutePath());

    }
}
