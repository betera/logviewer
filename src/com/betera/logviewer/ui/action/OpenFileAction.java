package com.betera.logviewer.ui.action;

import com.betera.logviewer.file.LogfilesContainer;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

public class OpenFileAction
        extends AbstractAction
{

    LogfilesContainer container;

    public OpenFileAction(LogfilesContainer container)
    {
        super("", new ImageIcon("./images/open.png"));
        this.container = container;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser openFileDialog = new JFileChooser();

        openFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        openFileDialog.setMultiSelectionEnabled(true);

        if ( openFileDialog.showOpenDialog(container.getComponent()) == JFileChooser.APPROVE_OPTION )
        {

            for ( File selectedFile : openFileDialog.getSelectedFiles() )
            {
                container.addFile(selectedFile);
            }
        }

    }
}
