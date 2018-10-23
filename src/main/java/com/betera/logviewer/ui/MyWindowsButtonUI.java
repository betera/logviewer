package com.betera.logviewer.ui;

import com.sun.java.swing.plaf.windows.WindowsButtonUI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

public class MyWindowsButtonUI
        extends WindowsButtonUI
{

    @Override
    protected void installDefaults(AbstractButton b)
    {
        UIManager.put(getPropertyPrefix() + "textShiftOffset", new Integer(1));
        super.installDefaults(b);
    }

    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect)
    {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        Icon icon = b.getIcon();
        Icon tmpIcon = null;

        if ( icon == null )
        {
            return;
        }

        Icon selectedIcon = null;

        /* the fallback icon should be based on the selected state */
        if ( model.isSelected() )
        {
            selectedIcon = b.getSelectedIcon();
            if ( selectedIcon != null )
            {
                icon = selectedIcon;
            }
        }

        if ( !model.isEnabled() )
        {
            if ( model.isSelected() )
            {
                tmpIcon = b.getDisabledSelectedIcon();
                if ( tmpIcon == null )
                {
                    tmpIcon = selectedIcon;
                }
            }

            if ( tmpIcon == null )
            {
                tmpIcon = b.getDisabledIcon();
            }
        }
        else if ( model.isPressed() && model.isArmed() )
        {
            tmpIcon = b.getPressedIcon();
            if ( tmpIcon != null )
            {
                // revert back to 0 offset
                clearTextShiftOffset();
            }
        }
        else if ( b.isRolloverEnabled() && model.isRollover() )
        {
            if ( model.isSelected() )
            {
                tmpIcon = b.getRolloverSelectedIcon();
                if ( tmpIcon == null )
                {
                    tmpIcon = selectedIcon;
                }
            }

            if ( tmpIcon == null )
            {
                tmpIcon = b.getRolloverIcon();
            }
        }

        if ( tmpIcon != null )
        {
            icon = tmpIcon;
        }

        if ( model.isPressed() && model.isArmed() )
        {
            setTextShiftOffset();
            icon.paintIcon(c, g, iconRect.x + getTextShiftOffset(), iconRect.y + getTextShiftOffset());
        }
        else
        {
            g.setColor(Color.LIGHT_GRAY);
            g.fillOval(iconRect.x + getTextShiftOffset(),
                       iconRect.y + getTextShiftOffset(),
                       iconRect.width,
                       iconRect.height);
            icon.paintIcon(c, g, iconRect.x, iconRect.y);
        }

    }

}
