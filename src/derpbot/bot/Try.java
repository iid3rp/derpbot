package derpbot.bot;

public class Try
{
    public static boolean parseInt(String message)
    {
        try
        {
            Integer.parseInt(message);
            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }
}
