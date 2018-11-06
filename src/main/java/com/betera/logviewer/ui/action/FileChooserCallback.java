package com.betera.logviewer.ui.action;

import java.io.File;

public interface FileChooserCallback
{

    File getDefaultPath();

    void elementSelected(File anElement);

    int getFileSelectionMode();
}
