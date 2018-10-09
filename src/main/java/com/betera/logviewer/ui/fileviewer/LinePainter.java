package com.betera.logviewer.ui.fileviewer;

import com.betera.logviewer.LogViewer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public class LinePainter
        implements Highlighter.HighlightPainter
{
    private JTextComponent component;

    private Color color;

    private int offset;

    private LinePainter master;

    /*
     *  Manually control the line color
     *
     *  @param component  text component that requires background line painting
     *  @param color      the color of the background line
     */
    public LinePainter(JTextComponent component, Color color, int offset, LinePainter master)
    {
        this.component = component;
        this.offset = offset;
        setColor(color);
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    /*
     *	You can reset the line color at any time
     *
     *  @param color  the color of the background line
     */
    public void setColor(Color color)
    {
        this.color = color;
    }

    private int getNewlineOffset(JTextComponent c, int offset)
    {
        String text = null;
        try
        {
            int len = 100;
            if ( (offset + len) > c.getDocument().getLength() )
            {
                len = c.getDocument().getLength() - offset;
            }
            text = c.getText(offset, len);
            int index = text.indexOf(System.getProperty("line.separator"));
            if ( index >= 0 )
            {
                return index + offset;
            }
            if ( len < 100 )
            {
                return offset + len;
            }
            return getNewlineOffset(c, offset + 100);
        }
        catch ( BadLocationException e )
        {
            LogViewer.handleException(e);
        }
        return -1;
    }

    //  Paint the background highlight

    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c)
    {
        try
        {
            Rectangle r = c.modelToView(offset);
            g.setColor(color);

            int nlOffset = getNewlineOffset(c, offset);
            Rectangle newRect = c.modelToView(nlOffset - 1);

            Rectangle union = r.union(newRect);

            if ( master != null && this != master )
            {
                if ( this.getOffset() == master.getOffset() )
                {
                    return;
                }
            }

            g.fillRect(0, r.y, c.getWidth(), union.height);
        }
        catch ( BadLocationException ble )
        {
            // ignore
        }
    }

}
