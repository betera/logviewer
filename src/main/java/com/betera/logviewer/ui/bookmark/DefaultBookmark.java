package com.betera.logviewer.ui.bookmark;

import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.file.highlight.HighlightEntry;
import javax.swing.tree.DefaultMutableTreeNode;

public class DefaultBookmark
        extends DefaultMutableTreeNode
        implements LogBookmark
{
    private Logfile logfile;
    private int offset;
    private String title;
    private HighlightEntry entry;
    private int row;

    public DefaultBookmark(Logfile logfile, int offset, int row, HighlightEntry entry, String title)
    {
        super();
        this.row = row;
        this.title = title;
        this.entry = entry;
        this.logfile = logfile;
        this.offset = offset;
    }

    @Override
    public String toString()
    {
        return title;
    }

    @Override
    public Logfile getLogfile()
    {
        return logfile;
    }

    @Override
    public String getName()
    {
        return logfile.getDisplayName();
    }

    @Override
    public int getOffset()
    {
        return offset;
    }

    @Override
    public int getRow()
    {
        return row;
    }
}
