package com.betera.logviewer.ui.bookmark;

import com.betera.logviewer.LogViewer;
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

    public DefaultBookmark(Logfile logfile, int offset, HighlightEntry entry, String title)
    {
        super();
        this.title = title;
        this.entry = entry;
        this.logfile = logfile;
        this.offset = offset;
    }

    @Override
    public String toString()
    {
        if ( title.length() > LogViewer.BOOKMARK_MAX_TITLE_SIZE )
        {
            title = title.substring(0, LogViewer.BOOKMARK_MAX_TITLE_SIZE - 3) + "...";
        }

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
}
