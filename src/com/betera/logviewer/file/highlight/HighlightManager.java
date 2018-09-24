package com.betera.logviewer.file.highlight;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class HighlightManager
{

    private static List<HighlightEntry> entries;
    private static HighlightEntry defaultEntry;

    public static void registerDefault(HighlightEntry entry)
    {
        defaultEntry = entry;
    }

    public static HighlightEntry getDefaultEntry()
    {
        if ( defaultEntry == null )
        {
            defaultEntry = new HighlightEntry("",
                                              new Font("Consolas", Font.PLAIN, 20),
                                              Color.BLACK,
                                              Color.WHITE,
                                              false);
        }
        return defaultEntry;
    }

    public static void registerHighlight(HighlightEntry entry)
    {
        if ( entries == null )
        {
            entries = new ArrayList<>();
        }

        entries.add(entry);
    }

    public static HighlightEntry findHighlightEntry(String text)
    {
        for ( HighlightEntry entry : entries )
        {
            if ( text.contains(entry.getText()) )
            {
                return entry;
            }
        }
        return null;
    }

}
