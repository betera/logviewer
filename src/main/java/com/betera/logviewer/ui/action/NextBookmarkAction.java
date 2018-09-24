package com.betera.logviewer.ui.action;

import com.betera.logviewer.ui.bookmark.BookmarkManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class NextBookmarkAction
        extends AbstractAction
{
    private BookmarkManager bookmarkManager;

    public NextBookmarkAction(BookmarkManager bookmarkManager)
    {
        super("", new ImageIcon("./images/arrowDown.png"));
        this.bookmarkManager = bookmarkManager;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        bookmarkManager.nextBookmark();
    }
}
