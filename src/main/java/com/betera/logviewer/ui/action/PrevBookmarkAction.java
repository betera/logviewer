package com.betera.logviewer.ui.action;

import com.betera.logviewer.ui.bookmark.BookmarkManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class PrevBookmarkAction
        extends AbstractAction
{
    private BookmarkManager bookmarkManager;

    public PrevBookmarkAction(BookmarkManager bookmarkManager)
    {
        super("", new ImageIcon("./images/arrowUp.png"));
        this.bookmarkManager = bookmarkManager;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        bookmarkManager.prevBookmark();
    }
}
