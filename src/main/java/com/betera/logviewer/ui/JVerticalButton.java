package com.betera.logviewer.ui;

import com.sun.java.swing.plaf.windows.WindowsToggleButtonUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import sun.swing.SwingUtilities2;

public class JVerticalButton
        extends JToggleButton
{
    Dimension d = new Dimension(32, 120);

    String realText;

    public JVerticalButton()
    {
        setSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);
        setFont(new Font("Segoe UI, Arial", Font.BOLD, 14));
        setFocusPainted(false);
        setContentAreaFilled(true);
    }

    @Override
    public void setText(String text)
    {
        realText = text;
        super.setText(text);
    }

    @Override
    public Dimension getPreferredSize()
    {
        return d;
    }

    @Override
    public Dimension getMinimumSize()
    {
        return d;
    }

    @Override
    public Dimension getMaximumSize()
    {
        return d;
    }

    @Override
    public Dimension getSize(Dimension rv)
    {
        return d;
    }

    @Override
    public void updateUI()
    {
        setUI(new VerticalToggleButtonUI());
    }

    protected class VerticalToggleButtonUI
            extends WindowsToggleButtonUI
    {

        private transient Color cachedSelectedColor = null;
        private transient Color cachedBackgroundColor = null;
        private transient Color cachedHighlightColor = null;

        @Override
        public void update(Graphics g, JComponent c)
        {
            paint(g, c);
        }

        protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text)
        {
            ButtonModel model = b.getModel();
            FontMetrics fm = SwingUtilities2.getFontMetrics(b, g);
            int mnemonicIndex = b.getDisplayedMnemonicIndex();

            Graphics2D g2d = (Graphics2D) g;

            /* Draw the Text */
            g2d.rotate(Math.PI / 2, 9, 18);
            if ( model.isEnabled() )
            {
                Rectangle2D stringBounds = g2d.getFontMetrics().getStringBounds(realText, g2d);
                /*** paint the text normally */
                g2d.setColor(b.getForeground());
                g2d.drawString(realText, (int) (50 - stringBounds.getWidth() / 2), 15);
            }
            else
            {
                /*** paint the text disabled ***/
                g.setColor(b.getBackground().brighter());
                g2d.drawString(realText,
                               textRect.x + getTextShiftOffset(),
                               textRect.y + fm.getAscent() + getTextShiftOffset());

                g.setColor(b.getBackground().darker());
                g2d.drawString(realText,
                               textRect.x + getTextShiftOffset() - 1,
                               textRect.y + fm.getAscent() + getTextShiftOffset() - 1);
            }

        }
    }
}
