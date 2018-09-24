package com.betera.logviewer.ui.bookmark;

import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.file.LogfilesContainer;
import com.betera.logviewer.ui.action.DeleteAllBookmarksAction;
import com.betera.logviewer.ui.action.NextBookmarkAction;
import com.betera.logviewer.ui.action.PrevBookmarkAction;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class BookmarkManager

{

    private static JPanel panel;
    private static JTree tree;
    private static DefaultMutableTreeNode rootNode;
    private static LogfilesContainer logContainer;

    private static TreeSelectionListener createListener()
    {
        return new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if ( node instanceof LogBookmark )
                {
                    LogBookmark bookmark = (LogBookmark) node;
                    logContainer.focusLogfile(bookmark.getLogfile(), bookmark.getOffset());
                }
            }
        };
    }

    private static void initPanel()
    {
        panel = new JPanel();
        panel.setPreferredSize(new Dimension(200, 200));

        rootNode = new DefaultMutableTreeNode("Bookmarks");

        tree = new JTree(rootNode);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(createListener());
        panel.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        panel.setLayout(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        toolbar.add(new DeleteAllBookmarksAction());
        toolbar.add(new PrevBookmarkAction());
        toolbar.add(new NextBookmarkAction());
        toolbar.setFloatable(false);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    private static void addToTree(LogBookmark bookmark)
    {
        for ( int i = 0; i < rootNode.getChildCount(); i++ )
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.getChildAt(i);

            Object userObject = node.getUserObject();
            if ( userObject instanceof Logfile )
            {
                Logfile log = (Logfile) userObject;
                if ( log.equals(bookmark.getLogfile()) )
                {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) bookmark;
                    node.add(child);
                    ((DefaultTreeModel) tree.getModel()).reload(rootNode);
                    tree.expandPath(new TreePath(node.getPath()));
                    tree.setSelectionPath(new TreePath(child.getPath()));
                    return;
                }
            }
        }

        DefaultMutableTreeNode logNode = new DefaultMutableTreeNode(bookmark.getLogfile(), true);

        rootNode.add(logNode);
        ((DefaultTreeModel) tree.getModel()).reload(logNode);
        addToTree(bookmark);
    }

    public static void addBookmark(LogBookmark bookmark)
    {
        addToTree(bookmark);
        tree.revalidate();
        tree.repaint();
    }

    public static JComponent getComponent()
    {
        if ( panel == null )
        {
            initPanel();
        }

        return panel;
    }

    public static void setLogContainer(LogfilesContainer logContainer)
    {
        BookmarkManager.logContainer = logContainer;
    }

    public static void deleteAllBookmarks()
    {
        rootNode.removeAllChildren();
        ((DefaultTreeModel) tree.getModel()).reload();
    }

    public static void nextBookmark()
    {
        TreePath selectionPath = tree.getSelectionPath();
        TreePath pathToSelect = null;
        if ( selectionPath == null )
        {
            pathToSelect = new TreePath(rootNode.getFirstLeaf());
        }
        else
        {

            Object lastPathComponent = selectionPath.getLastPathComponent();
            if ( lastPathComponent instanceof DefaultBookmark )
            {
                DefaultMutableTreeNode nextLeaf = ((DefaultBookmark) lastPathComponent).getNextLeaf();
                if ( nextLeaf != null )
                {
                    pathToSelect = new TreePath(nextLeaf.getPath());
                }
                else
                {
                    pathToSelect = new TreePath(rootNode.getFirstLeaf());
                }
            }
        }
        tree.setSelectionPath(pathToSelect);
    }

    public static void prevBookmark()
    {
        TreePath selectionPath = tree.getSelectionPath();
        TreePath pathToSelect = null;
        if ( selectionPath == null )
        {
            pathToSelect = new TreePath(rootNode.getFirstLeaf());
        }
        else
        {

            Object lastPathComponent = selectionPath.getLastPathComponent();
            if ( lastPathComponent instanceof DefaultBookmark )
            {
                DefaultMutableTreeNode prevLeaf = ((DefaultBookmark) lastPathComponent).getPreviousLeaf();
                if ( prevLeaf != null )
                {
                    pathToSelect = new TreePath(prevLeaf.getPath());
                }
                else
                {
                    pathToSelect = new TreePath(rootNode.getLastLeaf());
                }
            }
        }
        tree.setSelectionPath(pathToSelect);
    }

    public static void deleteLogfileBookmarks(Logfile file)
    {
        int indexToRemove = -1;

        for ( int i = 0; i < rootNode.getChildCount(); i++ )
        {
            DefaultMutableTreeNode tempNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            Logfile logfile = (Logfile) tempNode.getUserObject();
            if ( logfile.equals(file) )
            {
                indexToRemove = i;
                break;
            }
        }

        if ( indexToRemove >= 0 )
        {
            rootNode.remove(indexToRemove);
            ((DefaultTreeModel) tree.getModel()).reload();
        }

    }
}
