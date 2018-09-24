package com.betera.logviewer.file;

public interface LogfileStateChangedListener
{

    void contentChanged(boolean hasNewContent, Logfile logfile);

}
