package com.betera.logviewer.ui;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JPanel;

public class EnablementInheritingJPanel
        extends JPanel
{

    protected void doSetChildEnablement(Component comp, boolean isEnabled)
    {
        if ( comp instanceof Container )
        {
            for ( Component child : ((Container) comp).getComponents() )
            {
                doSetChildEnablement(child, isEnabled);
            }
        }
        comp.setEnabled(isEnabled);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        for ( Component comp : getComponents() )
        {
            doSetChildEnablement(comp, enabled);
        }
    }

}
