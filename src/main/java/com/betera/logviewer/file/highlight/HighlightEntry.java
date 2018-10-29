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

    public void setAddBookmark(boolean addBookmark)
    {
        this.addBookmark = addBookmark;
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

    public void setText(String text)
    {
        this.text = text;
    }

    public Font getFont()
    {
        return font != null ? font : new Font("Consolas", Font.PLAIN, 14);
    }

    public void setFont(Font font)
    {
        this.font = font;
    }

    public void setBold(boolean isBold)
    {
        setFont(getFont().deriveFont(isBold ? Font.BOLD : Font.PLAIN));
    }

    public void setFontFamily(String family)
    {
        setFont(new Font(family, getFont().getStyle(), getFont().getSize()));
    }

    public void setFontSize(int size)
    {
        setFont(getFont().deriveFont((float) size));
    }

    public HighlightEntry copy()
    {
        return new HighlightEntry(text, font, foregroundColor, backgroundColor, addBookmark);
    }
}
