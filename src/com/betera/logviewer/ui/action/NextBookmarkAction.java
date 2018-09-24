package com.betera.logviewer.ui.action;

import com.betera.logviewer.ui.bookmark.BookmarkManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class NextBookmarkAction
        extends AbstractAction
{
    public NextBookmarkAction()
    {
        super("", new ImageIcon("./images/arrowDown.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        BookmarkManager.nextBookmark();
    }
}
