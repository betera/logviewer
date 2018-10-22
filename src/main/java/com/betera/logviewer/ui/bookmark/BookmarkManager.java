package com.betera.logviewer.ui.bookmark;

import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.ui.action.DeleteAllBookmarksAction;
import com.betera.logviewer.ui.action.NextBookmarkAction;
import com.betera.logviewer.ui.action.PrevBookmarkAction;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.swingx.JXFindBar;
import org.jdesktop.swingx.JXTree;

public class BookmarkManager

{

    private JPanel panel;
    private JXTree tree;
    private DefaultMutableTreeNode rootNode;
    private Logfile logfile;
    private boolean inPopulate = false;

    public BookmarkManager(Logfile logfile)
    {
        this.logfile = logfile;
    }

    private TreeSelectionListener createListener()
    {
        return new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                if ( inPopulate )
                {
                    return;
                }

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if ( node instanceof LogBookmark )
                {
                    LogBookmark bookmark = (LogBookmark) node;
                    logfile.scrollTo(bookmark.getOffset(), bookmark.getRow(), true);
                }
            }
        };
    }

    private void initPanel()
    {
        panel = new JPanel();
        panel.setPreferredSize(new Dimension(200, 800));

        rootNode = new DefaultMutableTreeNode(logfile.getDisplayName());
        rootNode.setUserObject(logfile);

        tree = new JXTree(rootNode);
        JXFindBar findBar = new JXFindBar(tree.getSearchable())
        {
            protected void build()
            {
                setLayout(new FlowLayout(SwingConstants.LEADING));
                add(searchField);
                add(findPrevious);
                add(findNext);
            }

            protected void bind()
            {
                super.bind();
                searchField.setColumns(10);
                findNext.setText(">>");
                findPrevious.setText("<<");
            }
        };
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(createListener());
        panel.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        panel.setLayout(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new GridLayout(0, 1));
        JToolBar actionBar = new JToolBar();

        actionBar.add(new DeleteAllBookmarksAction(this));
        actionBar.add(new PrevBookmarkAction(this));
        actionBar.add(new NextBookmarkAction(this));
        toolbar.add(actionBar);
        toolbar.add(findBar);
        toolbar.setFloatable(false);
        actionBar.setFloatable(false);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    private void addToTree(LogBookmark bookmark)
    {
        if ( logfile.equals(bookmark.getLogfile()) )
        {
            inPopulate = true;
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) bookmark;
            rootNode.add(child);
            ((DefaultTreeModel) tree.getModel()).reload(rootNode);
            tree.expandPath(new TreePath(rootNode.getPath()));
            tree.setSelectionPath(new TreePath(child.getPath()));
            inPopulate = false;
            return;
        }

    }

    public void addBookmark(LogBookmark bookmark)
    {
        addToTree(bookmark);
        tree.revalidate();
        tree.repaint();
    }

    public JComponent getComponent()
    {
        if ( panel == null )
        {
            initPanel();
        }

        return panel;
    }

    public void deleteAllBookmarks()
    {
        rootNode.removeAllChildren();
        ((DefaultTreeModel) tree.getModel()).reload();
    }

    public void nextBookmark()
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

    public void prevBookmark()
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

}
