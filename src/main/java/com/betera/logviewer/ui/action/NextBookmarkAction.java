package com.betera.logviewer.ui.action;

import com.betera.logviewer.Icons;
import com.betera.logviewer.ui.bookmark.BookmarkManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class NextBookmarkAction
        extends AbstractAction
{
    private BookmarkManager bookmarkManager;

    public NextBookmarkAction(BookmarkManager bookmarkManager)
    {
        super("", Icons.arrowDownIcon);
        this.bookmarkManager = bookmarkManager;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        bookmarkManager.nextBookmark();
    }
}
