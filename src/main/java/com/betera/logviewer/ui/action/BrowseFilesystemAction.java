package com.betera.logviewer.ui.action;

import com.betera.logviewer.Icons;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

public class BrowseFilesystemAction
        extends AbstractAction
{

    private FileChooserCallback cb;

    public BrowseFilesystemAction(FileChooserCallback cb)
    {
        super("Browse", Icons.browseIcon);
        this.cb = cb;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser fc = new JFileChooser(cb.getDefaultPath());
        fc.setFileSelectionMode(cb.getFileSelectionMode());
        fc.setMultiSelectionEnabled(false);
        int i = fc.showOpenDialog(null);
        if ( i == JFileChooser.APPROVE_OPTION )
        {
            cb.elementSelected(fc.getSelectedFile());
        }
    }
}
