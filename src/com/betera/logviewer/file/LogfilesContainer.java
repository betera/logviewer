package com.betera.logviewer.file;

import java.io.File;
import java.util.List;
import javax.swing.JComponent;

public interface LogfilesContainer
{

    JComponent getComponent();

    void addFile(File file);

    void removeLogfile(Logfile file);

    void fireFollowTailChanged(boolean doFollowTail, Logfile ignored);

    void updateFollowTailCheckbox(boolean doFollowTail, Logfile source);

    void focusLogfile(Logfile file, int offset);

    List<Logfile> getOpenLogfiles();

    boolean isLogfileFocused(Logfile aLogfile);

}
