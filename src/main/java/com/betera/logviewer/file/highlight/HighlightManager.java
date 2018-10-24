package com.betera.logviewer.file.highlight;

import com.betera.logviewer.LogViewer;
import com.betera.logviewer.ui.edit.ConfigDialog;
import com.betera.logviewer.ui.edit.ConfigEditUIProvider;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class HighlightManager
        implements ConfigEditUIProvider
{

    private static HighlightManager INSTANCE = new HighlightManager();
    private List<HighlightEntry> entryList;
    private HighlightEntry defaultEntry;
    private HighlightEditPanel editPanel;

    public static synchronized HighlightManager getInstance()
    {
        return INSTANCE;
    }

    public List<HighlightEntry> getEntries()
    {
        return entryList;
    }

    public void registerDefault(HighlightEntry entry)
    {
        defaultEntry = entry;
    }

    public HighlightEntry getDefaultEntry()
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

    public void registerHighlight(HighlightEntry entry)
    {
        if ( entryList == null )
        {
            entryList = new ArrayList<>();
        }

        entryList.add(entry);
    }

    public void saveHighlightConfig()
            throws IOException
    {
        File f = new File("highlight.config");
        if ( f.exists() )
        {
            f.delete();
        }
        f.createNewFile();
        try (PrintWriter out = new PrintWriter(new FileWriter(f)))
        {
            out.println("[Highlight ###DEFAULT###]");
            printToConfig(out, getDefaultEntry());
            for ( HighlightEntry entry : getEntries() )
            {
                out.println("[Highlight " + entry.getText() + "]");
                printToConfig(out, entry);
            }
        }
    }

    private void printToConfig(PrintWriter out, HighlightEntry entry)
    {
        out.println(entry.getFont().getFamily());
        out.print(entry.getFont().getStyle() + " ");
        out.print(entry.getFont().getSize() + " ");
        out.print(colorToString(entry.getForegroundColor()) + " ");
        out.print(colorToString(entry.getBackgroundColor()) + " ");
        out.println(entry.isAddBookmark() ? "1" : "0");

    }

    private String colorToString(Color aColor)
    {
        return "#" + n(Integer.toHexString(aColor.getRed())) + n(Integer.toHexString(aColor.getGreen()))
                + n(Integer.toHexString(aColor.getBlue()));
    }

    private String n(String t)
    {
        String n = t;
        while ( n.length() < 2 )
        {
            n = "0" + n;
        }
        return n;
    }

    public void readHighlightConfig()
            throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("highlight.config"))))
        {
            String line = reader.readLine();
            while ( line != null )
            {
                if ( line.startsWith("[Highlight ") )
                {
                    String text = line.substring(line.indexOf(' ') + 1, line.length() - 1);
                    boolean isDefault = text.equals("###DEFAULT###");
                    String fontName = reader.readLine();
                    line = reader.readLine();
                    String[] stuff = line.split(" ");
                    String sFontType = stuff[0];
                    String sFontSize = stuff[1];
                    String foregroundColor = stuff[2];
                    String backgroundColor = stuff[3];
                    String sAddBookmark = "0";
                    if ( stuff.length >= 5 )
                    {
                        sAddBookmark = stuff[4];
                    }

                    int fontType = Integer.valueOf(sFontType);
                    int fontSize = Integer.valueOf(sFontSize);
                    javafx.scene.paint.Color fgColor = javafx.scene.paint.Color.valueOf(foregroundColor);
                    javafx.scene.paint.Color bgColor = javafx.scene.paint.Color.valueOf(backgroundColor);
                    boolean addBookmark = "1".equals(sAddBookmark);

                    Font font = new Font(fontName, fontType, fontSize);
                    HighlightEntry entry = new HighlightEntry(text,
                                                              font,
                                                              new java.awt.Color((float) fgColor.getRed(),
                                                                                 (float) fgColor.getGreen(),
                                                                                 (float) fgColor.getBlue()),
                                                              new java.awt.Color((float) bgColor.getRed(),
                                                                                 (float) bgColor.getGreen(),
                                                                                 (float) bgColor.getBlue()),
                                                              addBookmark);

                    if ( isDefault )
                    {
                        registerDefault(entry);
                    }
                    else
                    {
                        registerHighlight(entry);
                    }
                }
                line = reader.readLine();
            }

        }

    }

    public HighlightEntry findHighlightEntry(String text)
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

    private Font getBiggestFont(Font f1, Font f2)
    {
        return f1.getSize() > f2.getSize()
                ? f1
                : (f2.getSize() > f1.getSize()
                        ? f2
                        : (f1.getStyle() == Font.BOLD ? f1 : (f1.getStyle() == Font.ITALIC ? f1 : f2)));
    }

    @Override
    public void displayEditPanel()
    {
        editPanel = new HighlightEditPanel();
        new ConfigDialog(this, editPanel).setVisible(true);
    }

    @Override
    public void updateConfig()
    {
        if ( entryList != null )
        {
            entryList.clear();
        }
        for ( HighlightEntry e : editPanel.getHighlightEntries() )
        {
            registerHighlight(e);
        }
        try
        {
            saveHighlightConfig();
        }
        catch ( IOException e )
        {
            LogViewer.handleException(e);
        }
    }
}
