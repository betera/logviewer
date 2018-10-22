package com.betera.logviewer.ui.action;

import com.betera.logviewer.ui.edit.ConfigEditUIProvider;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class EditAction
        extends AbstractAction
{
    private ConfigEditUIProvider editProvider;

    public EditAction(String title, ConfigEditUIProvider editProvider)
    {
        super(title);
        this.editProvider = editProvider;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        editProvider.displayEditPanel();
    }
}
