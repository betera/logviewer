package com.betera.logviewer.file;

import java.awt.Font;
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

    List<Logfile> getOpenLogfiles();

    boolean isLogfileFocused(Logfile aLogfile);

    void focusLogfile(Logfile aLogfile);

    void defaultFontChanged(Font newFont);

}
