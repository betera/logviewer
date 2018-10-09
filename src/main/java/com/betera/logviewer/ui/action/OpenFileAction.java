package com.betera.logviewer.ui.action;

import com.betera.logviewer.LogViewer;
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

    LogViewer logViewer;

    public OpenFileAction(LogViewer logViewer, LogfilesContainer container)
    {
        super("Open file...", new ImageIcon("./images/open.png"));
        this.container = container;
        this.logViewer = logViewer;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String path = logViewer.getRecentFiles().size() > 0 ? logViewer.getRecentFiles().get(
                logViewer.getRecentFiles().size() - 1) : "";

        JFileChooser openFileDialog = new JFileChooser(path);

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
