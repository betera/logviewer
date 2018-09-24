package com.betera.logviewer.ui.action;

import com.betera.logviewer.ui.bookmark.BookmarkManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class PrevBookmarkAction
        extends AbstractAction
{
    public PrevBookmarkAction()
    {
        super("", new ImageIcon("./images/arrowUp.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        BookmarkManager.prevBookmark();
    }
}
