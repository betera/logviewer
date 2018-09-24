package com.betera.logviewer.file.highlight;

import java.awt.Color;
import java.awt.Font;

public class HighlightEntry
{

    private String text;
    private Color foregroundColor;
    private Color backgroundColor;
    private Font font;
    private boolean addBookmark;

    public HighlightEntry(String text, Font font, Color foregroundColor, Color backgroundColor, boolean addBookmark)
    {
        this.text = text;
        this.font = font;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.addBookmark = addBookmark;
    }

    public boolean isAddBookmark()
    {
        return addBookmark;
    }

    public Color getForegroundColor()
    {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor)
    {
        this.foregroundColor = foregroundColor;
    }

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    public String getText()
    {
        return text;
    }

    public Font getFont()
    {
        return font != null ? font : new Font("Consolas", Font.PLAIN, 14);
    }
}
