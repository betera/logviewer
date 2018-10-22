package com.betera.logviewer.ui.edit;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public abstract class DocumentTextAdapter
        implements DocumentListener
{

    public abstract void textChanged(String text);

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        try
        {
            textChanged(e.getDocument().getText(0, e.getDocument().getLength()));
        }
        catch ( BadLocationException e1 )
        {
            e1.printStackTrace();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        insertUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        insertUpdate(e);
    }
}
