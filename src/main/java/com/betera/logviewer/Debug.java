package com.betera.logviewer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Debug
{

    static boolean enabled = false;

    static long startTime;

    static String text;

    static long endTime;

    static Map<String, Integer> map = new HashMap<>();

    public static void start(String text)
    {
        if ( !enabled )
        {
            return;
        }
        Debug.text = text;
        startTime = System.nanoTime();
    }

    public static void end()
    {
        if ( !enabled )
        {
            return;
        }
        endTime = System.nanoTime();
        if ( !map.containsKey(text) )
        {
            map.put(text, 0);
        }
        map.put(text, map.get(text) + 1);
        System.out.println("Debug[" + text + "] -> " + (((endTime - startTime) / 1000.0)));
    }

    public static void printStatistics()
    {
        if ( !enabled )
        {
            return;
        }
        for ( Entry entry : map.entrySet() )
        {
            System.out.println("Called " + entry.getKey() + " x " + entry.getValue());
        }
    }

}
