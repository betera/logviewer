package com.betera.logviewer.file;

import javax.swing.JComponent;

public interface Logfile
{

    LogfilesContainer getContainer();

    LogfileConfiguration getConfiguration();

    JComponent getComponent();

    void followTailChanged(boolean doFollowTail);

    void updateFollowTailCheckbox(boolean doFollowTail);

    void addLogfileStateChangedListener(LogfileStateChangedListener listener);

    byte[] getBytes();

    void destroy();

    String getName();

    String getDisplayName();

    String getAbsolutePath();

    void dispose();

    String toString();

    void scrollTo(int offset);

    void processLine(String aLine);
}
