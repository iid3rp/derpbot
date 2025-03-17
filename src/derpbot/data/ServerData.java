package derpbot.data;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

// might sit and podnder about this...
public class ServerData extends ConcurrentHashMap<String, ConcurrentHashMap<String, LinkedList<String>>>
{
    public ServerData()
    {

    }
}
