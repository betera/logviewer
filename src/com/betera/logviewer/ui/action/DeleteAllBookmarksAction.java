package com.betera.logviewer.ui.action;

import com.betera.logviewer.ui.bookmark.BookmarkManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class DeleteAllBookmarksAction
        extends AbstractAction
{

    public DeleteAllBookmarksAction()
    {
        super("", new ImageIcon("./images/deleteAll.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        BookmarkManager.deleteAllBookmarks();
    }
}
