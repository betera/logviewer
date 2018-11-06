package com.betera.logviewer.ui.action;

import com.betera.logviewer.Icons;
import com.betera.logviewer.ui.bookmark.BookmarkManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class PrevBookmarkAction
        extends AbstractAction
{
    private BookmarkManager bookmarkManager;

    public PrevBookmarkAction(BookmarkManager bookmarkManager)
    {
        super("", Icons.arrowUpIcon);
        this.bookmarkManager = bookmarkManager;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        bookmarkManager.prevBookmark();
    }
}
