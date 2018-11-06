package com.betera.logviewer.ui.action;

import java.io.File;
import javax.swing.JFileChooser;

public class FileChooserCallbackAdapter
        implements FileChooserCallback
{
    @Override
    public File getDefaultPath()
    {
        return null;
    }

    @Override
    public void elementSelected(File anElement)
    {

    }

    @Override
    public int getFileSelectionMode()
    {
        return JFileChooser.DIRECTORIES_ONLY;
    }
}
