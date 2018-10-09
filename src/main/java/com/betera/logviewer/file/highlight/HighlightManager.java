package com.betera.logviewer.file.highlight;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class HighlightManager
{

    private static List<HighlightEntry> entryList;
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
        if ( entryList == null )
        {
            entryList = new ArrayList<>();
        }

        entryList.add(entry);
    }

    public static HighlightEntry findHighlightEntry(String text)
    {
        List<HighlightEntry> entries = new ArrayList<>();

        for ( HighlightEntry entry : entryList )
        {
            if ( text.contains(entry.getText()) )
            {
                entries.add(entry);
            }
        }

        if ( entries.isEmpty() )
        {
            return null;
        }
        else if ( entries.size() == 1 )
        {
            return entries.get(0);
        }
        else
        {
            // merge colors
            Font font = null;
            boolean addBookmark = false;
            int fr = 0;
            int fg = 0;
            int fb = 0;
            int br = 0;
            int bg = 0;
            int bb = 0;
            String newText = entries.get(0).getText();

            for ( HighlightEntry entry : entries )
            {
                if ( font == null )
                {
                    font = entry.getFont();
                }
                else
                {
                    font = getBiggestFont(font, entry.getFont());
                }
                addBookmark = addBookmark || entry.isAddBookmark();
                fr += entry.getForegroundColor().getRed();
                fg += entry.getForegroundColor().getGreen();
                fb += entry.getForegroundColor().getBlue();
                br += entry.getBackgroundColor().getRed();
                bg += entry.getBackgroundColor().getGreen();
                bb += entry.getBackgroundColor().getBlue();
            }

            Color foregroundColor = new Color(fr / entries.size(), fg / entries.size(), fb / entries.size());
            Color backgroundColor = new Color(br / entries.size(), bg / entries.size(), bb / entries.size());

            float[] hsbFG = Color.RGBtoHSB((int) foregroundColor.getRed(),
                                           (int) foregroundColor.getGreen(),
                                           (int) foregroundColor.getBlue(),
                                           null);
            float[] hsbBG = Color.RGBtoHSB((int) backgroundColor.getRed(),
                                           (int) backgroundColor.getGreen(),
                                           (int) backgroundColor.getBlue(),
                                           null);
            // use black or white if contrast is too low
            if ( hsbFG[2] - hsbBG[2] < 128 )
            {
                if ( hsbBG[2] > 128 )
                {
                    foregroundColor = Color.BLACK;
                }
                else
                {
                    foregroundColor = Color.WHITE;
                }
            }

            return new HighlightEntry(newText, font, foregroundColor, backgroundColor, addBookmark);
        }
    }

    private static Font getBiggestFont(Font f1, Font f2)
    {
        return f1.getSize() > f2.getSize()
                ? f1
                : (f2.getSize() > f1.getSize()
                        ? f2
                        : (f1.getStyle() == Font.BOLD ? f1 : (f1.getStyle() == Font.ITALIC ? f1 : f2)));
    }

}
