package com.betera.logviewer.ui.bookmark;

import com.betera.logviewer.file.Logfile;
import javax.swing.tree.TreeNode;

public interface LogBookmark
        extends TreeNode
{

    Logfile getLogfile();

    String getName();

    int getOffset();

    int getRow();

}
