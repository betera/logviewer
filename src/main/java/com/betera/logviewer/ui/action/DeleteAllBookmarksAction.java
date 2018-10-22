package com.betera.logviewer.ui.action;

import com.betera.logviewer.ui.bookmark.BookmarkManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class DeleteAllBookmarksAction
        extends AbstractAction
{

    private BookmarkManager bookmarkManager;

    public DeleteAllBookmarksAction(BookmarkManager bookmarkManager)
    {
        super("", new ImageIcon("./images/trashbin.png"));
        this.bookmarkManager = bookmarkManager;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        bookmarkManager.deleteAllBookmarks();
    }
}
