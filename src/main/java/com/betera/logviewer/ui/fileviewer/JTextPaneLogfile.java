package com.betera.logviewer.ui.fileviewer;

import com.betera.logviewer.LogViewer;
import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.file.LogfileConfiguration;
import com.betera.logviewer.file.LogfileStateChangedListener;
import com.betera.logviewer.file.LogfilesContainer;
import com.betera.logviewer.file.column.LogfileColumn;
import com.betera.logviewer.file.column.LogfileColumnConfig;
import com.betera.logviewer.file.column.LogfileColumnConfigEntry;
import com.betera.logviewer.file.column.LogfileParser;
import com.betera.logviewer.file.highlight.HighlightEntry;
import com.betera.logviewer.file.highlight.HighlightManager;
import com.betera.logviewer.ui.JVerticalButton;
import com.betera.logviewer.ui.bookmark.BookmarkManager;
import com.betera.logviewer.ui.bookmark.DefaultBookmark;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;

public class JTextPaneLogfile
        extends MouseAdapter
        implements Logfile, TableCellRenderer
{

    private File file;
    private JScrollPane scrollPane;
    private JTextPane textPane;
    private LogfileConfiguration config;
    private String newLine;
    private StyledDocument doc;

    private LinePainter selectPainter = null;
    private Object selectHighlightInfo;
    private boolean followTail;
    private Color selectionColor;
    private JPanel toolsPanel;
    private LogfilesContainer container;
    private BookmarkManager bookmarkManager;
    private JSplitPane splitter;
    private ButtonGroup toolWindowButtonGroup;
    private JPanel toolBarContentPanel;
    private JXTable docTable;
    private LogfileColumnConfig columnConfig;
    private float minLabelSpan;

    private boolean lineWrap = false;
    private List<LogfileStateChangedListener> listener;
    private WatchService watcher;
    private WatchKey register;

    private JLabel waitLabel;
    private boolean isTextView = true;
    private JToolBar contextToolBar;
    private JPanel leftTBPanel;

    public JTextPaneLogfile(LogfilesContainer container, File aFile, LogfileConfiguration config)
    {
        selectionColor = new Color(51, 153, 255, 255);

        listener = new ArrayList<>();
        file = aFile;
        followTail = true;
        this.container = container;
        this.config = config;
        bookmarkManager = new BookmarkManager(this);

        initTextPane();

        initHighlighting();

        initTable();

        initToolWindows();
        initSplitterAndScrollPane();

        initReaderThread();
    }

    private MouseWheelListener createScrollListener()
    {
        return new MouseWheelListener() // NOSONAR
        {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                boolean scrolledDown = e.getPreciseWheelRotation() > 0;

                JScrollBar vsb = scrollPane.getVerticalScrollBar();

                int value = vsb.getValue() + vsb.getModel().getExtent();
                int max = vsb.getMaximum();

                if ( !scrolledDown )
                {
                    if ( followTail )
                    {
                        updateFollowTailCheckbox(false);
                    }
                    followTail = false;
                }
                else if ( value >= max )
                {
                    if ( !followTail )
                    {
                        updateFollowTailCheckbox(true);
                    }
                    followTail = true;
                }
            }

        };
    }

    private void initReaderThread()
    {
        Thread readerThread = new Thread(createReaderThreadRunnable());
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void initSplitterAndScrollPane()
    {
        scrollPane = new JScrollPane()
        {
            public JScrollBar createVerticalScrollBar()
            {
                ScrollBar scrollBar = new ScrollBar(JScrollBar.VERTICAL);
                return scrollBar;
            }

        };
        scrollPane.setViewportView(textPane);
        scrollPane.addMouseWheelListener(createScrollListener());

        JPanel logRoot = new JPanel();
        logRoot.setLayout(new BorderLayout());

        logRoot.add(createLogfileToolbar(), BorderLayout.NORTH);
        logRoot.add(scrollPane, BorderLayout.CENTER);
        splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logRoot, toolsPanel);
        splitter.setBorder(new LineBorder(Color.GRAY));

        splitter.setDividerSize(8);
        splitter.setResizeWeight(0.8);

        scrollPane.setViewportView(textPane);
    }

    private void initToolWindows()
    {
        JToolBar toolsToolbar = new JToolBar();
        toolBarContentPanel = new JPanel();
        toolBarContentPanel.setLayout(new GridBagLayout());
        toolsToolbar.setLayout(new BorderLayout());
        toolsToolbar.add(toolBarContentPanel, BorderLayout.NORTH);
        toolsToolbar.add(new JPanel(), BorderLayout.CENTER);
        toolsToolbar.setOrientation(JToolBar.VERTICAL);
        toolsToolbar.setBorderPainted(true);

        toolsPanel = new JPanel();
        toolsPanel.setVisible(false);
        JPanel taskContainer = new JPanel();
        taskContainer.setLayout(new BorderLayout());
        toolsPanel.setLayout(new BorderLayout());
        toolsPanel.add(toolsToolbar, BorderLayout.EAST);
        toolsPanel.add(taskContainer, BorderLayout.CENTER);
        createToolWindowButton(taskContainer, bookmarkManager.getComponent(), "Bookmarks");
        createToolWindowButton(taskContainer, new JPanel(), "Test");
        toolsToolbar.setFloatable(false);
        toolsToolbar.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        taskContainer.add(bookmarkManager.getComponent());
    }

    private void initTable()
    {
        docTable = new JXTable();
        docTable.setSortable(false);
        docTable.setGridColor(Color.lightGray);
        docTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        columnConfig = LogfileParser.findMatchingConfig(this);
        createTableModelFromColumnConfig();

    }

    private void initHighlighting()
    {
        DefaultHighlighter highlighter = new DefaultHighlighter();
        highlighter.setDrawsLayeredHighlights(false);
        textPane.setHighlighter(highlighter);
        selectPainter = new LinePainter(textPane, selectionColor, 0, null);
    }

    private DocumentListener createDocumentScrollListener()
    {
        return new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                if ( followTail && textPane.getDocument().equals(doc) )
                {
                    scrollToEnd();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {

            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {

            }
        };
    }

    private void initTextPane()
    {
        textPane = new JTextPane()
        {
            @Override
            public void scrollRectToVisible(Rectangle aRect)
            {
                if ( followTail )
                {
                    if ( aRect != null )
                    {
                        super.scrollRectToVisible(aRect);
                    }
                }
            }
        };
        textPane.setBorder(BorderFactory.createEtchedBorder());
        textPane.setEditable(false);
        textPane.addMouseListener(this);
        textPane.setEditorKit(new WrapEditorKit());
        textPane.setBackground(HighlightManager.getDefaultEntry().getBackgroundColor());
        doc = textPane.getStyledDocument();
        doc.addDocumentListener(createDocumentScrollListener());
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textPane.setFont(new Font("Consolas", Font.PLAIN, 18));

    }

    private JToolBar createTextViewToolbar()
    {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        return tb;
    }

    private int getMaxWidthForColumn(TableColumn col)
    {
        for ( LogfileColumnConfigEntry entry : columnConfig.getEntries() )
        {
            if ( col.getIdentifier().equals(entry.getColumnName()) )
            {
                return entry.getMaxColumnSize();
            }
        }
        return 0;
    }

    private JToolBar createTableViewToolbar()
    {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tb.add(new AbstractAction("Pack table", new ImageIcon("./images/pack.png"))
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ( lineWrap )
                {
                    docTable.setHorizontalScrollEnabled(true);
                }
                docTable.packAll();
                if ( lineWrap )
                {
                    docTable.setHorizontalScrollEnabled(false);
                }
            }
        });
        Icon lineWrapOnIcon = new ImageIcon("./images/arrowDown.png");
        Icon lineWrapOffIcon = new ImageIcon("./images/arrowUp.png");

        tb.add(new AbstractAction("Toggle Line-Wrap", lineWrapOnIcon)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ( lineWrap )
                {
                    lineWrap = false;
                    docTable.setHorizontalScrollEnabled(true);
                    putValue(Action.SMALL_ICON, lineWrapOnIcon);
                }
                else
                {
                    lineWrap = true;
                    docTable.setHorizontalScrollEnabled(false);
                    putValue(Action.SMALL_ICON, lineWrapOffIcon);
                }
                if ( followTail )
                {
                    scrollToEnd();
                }
            }
        });

        return tb;

    }

    private JToolBar createSharedToolbar()
    {
        final ImageIcon tableIcon = new ImageIcon("./images/table.png");
        final ImageIcon textPaneIcon = new ImageIcon("./images/textPane.png");

        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tb.add(new AbstractAction("Table view", new ImageIcon("./images/table.png"))
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Component comp = scrollPane.getViewport().getView();
                if ( comp instanceof JXTable )
                {
                    scrollPane.setViewportView(textPane);
                    isTextView = true;
                    leftTBPanel.remove(contextToolBar);
                    contextToolBar = createTextViewToolbar();
                    leftTBPanel.add(contextToolBar);
                    putValue(Action.SMALL_ICON, tableIcon);
                }
                else
                {
                    scrollPane.setViewportView(docTable);
                    isTextView = false;
                    leftTBPanel.remove(contextToolBar);
                    contextToolBar = createTableViewToolbar();
                    leftTBPanel.add(contextToolBar);
                    putValue(Action.SMALL_ICON, textPaneIcon);
                }
                leftTBPanel.revalidate();
                leftTBPanel.repaint();
            }
        });

        return tb;
    }

    private JToolBar createLogfileToolbar()
    {
        contextToolBar = createTextViewToolbar();

        JToolBar logRootToolBar = new JToolBar();
        logRootToolBar.setLayout(new BorderLayout());
        JToolBar rightTB = new JToolBar();
        rightTB.setFloatable(false);

        leftTBPanel = new JPanel();
        leftTBPanel.setLayout(new FlowLayout());
        leftTBPanel.add(createSharedToolbar());
        leftTBPanel.add(contextToolBar);
        logRootToolBar.add(leftTBPanel, BorderLayout.WEST);
        logRootToolBar.add(rightTB, BorderLayout.EAST);

        Icon toolPanelOn = new ImageIcon("./images/arrowDown.png");
        Icon toolPanelOff = new ImageIcon("./images/arrowUp.png");
        rightTB.add(new AbstractAction("Toggle tool panel", toolPanelOn)
        {
            int divider = -1;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ( toolsPanel.isVisible() )
                {
                    divider = splitter.getDividerLocation();
                }
                toolsPanel.setVisible(!toolsPanel.isVisible());
                putValue(Action.SMALL_ICON, toolsPanel.isVisible() ? toolPanelOff : toolPanelOn);
                if ( toolsPanel.isVisible() )
                {
                    if ( divider == -1 )
                    {
                        divider = (int) (splitter.getVisibleRect().width * splitter.getResizeWeight());
                    }

                    splitter.setDividerLocation(divider);
                }

            }
        });

        logRootToolBar.setFloatable(false);
        return logRootToolBar;
    }

    private void createTableModelFromColumnConfig()
    {
        docTable.setDefaultRenderer(Object.class, this);
        docTable.setEditable(false);
        docTable.setHorizontalScrollEnabled(true);

        docTable.setColumnControlVisible(true);

        if ( columnConfig == null )
        {
            ((DefaultTableModel) docTable.getModel()).setColumnIdentifiers(new String[] { "Line", "Text" });
            return;
        }

        String[] cols = new String[1 + columnConfig.getEntries().length];
        cols[0] = "Line";
        for ( int i = 0; i < columnConfig.getEntries().length; i++ )
        {
            cols[i + 1] = columnConfig.getEntries()[i].getColumnName();
        }

        ((DefaultTableModel) docTable.getModel()).setColumnIdentifiers(cols);
        DefaultTableColumnModelExt mdl = (DefaultTableColumnModelExt) docTable.getColumnModel();
        mdl.getColumnExt("Line").setMaxWidth(10 * 10);
        List<TableColumn> columns = mdl.getColumns(true);
        for ( TableColumn column : columns )
        {
            String identifier = (String) column.getIdentifier();
            {
                for ( LogfileColumnConfigEntry entry : columnConfig.getEntries() )
                {
                    if ( entry.getColumnName().equals(identifier) )
                    {
                        int maxWidth = entry.getMaxColumnSize() * 10;
                        mdl.getColumnExt(identifier).setPreferredWidth(maxWidth);
                        if ( entry.isInitiallyHidden() )
                        {
                            mdl.getColumnExt(identifier).setVisible(false);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void createToolWindowButton(JPanel container, JComponent aComp, String aTitle)
    {

        JToggleButton btn = new JVerticalButton();
        btn.setText(aTitle);
        aComp.setBorder(new EmptyBorder(1, 1, 1, 1));
        btn.addActionListener(e -> {
            container.removeAll();
            container.add(aComp, BorderLayout.CENTER);
            container.revalidate();
            container.repaint();
        });
        btn.setBorderPainted(true);
        btn.setMargin(new Insets(4, 4, 4, 4));

        if ( toolWindowButtonGroup == null )
        {
            toolWindowButtonGroup = new ButtonGroup();
        }
        toolWindowButtonGroup.add(btn);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        toolBarContentPanel.add(btn, c);
    }

    @Override
    public String toString()
    {
        return getDisplayName();
    }

    @Override
    public void scrollTo(int offset, int row, boolean setSelection)
    {
        if ( isTextView && textPane.getDocument().equals(doc) )
        {
            textPane.setCaretPosition(offset);
            try
            {
                if ( doc.getLength() > 0 )
                {
                    textPane.scrollRectToVisible(textPane.modelToView(offset - 1));
                }
            }
            catch ( BadLocationException e )
            {
                LogViewer.handleException(e);
            }
        }
        else
        {
            docTable.getSelectionModel().setSelectionInterval(row + 1, row + 1);
            docTable.scrollRowToVisible(row + 1);
        }
        if ( setSelection )
        {
            mouseClicked(null);
        }
    }

    private void setStyle(HighlightEntry entry, int start, int end)
    {
        Font font = entry.getFont();
        Color c = entry.getForegroundColor();

        MutableAttributeSet attrs = textPane.getInputAttributes();
        if ( font != null )
        {
            StyleConstants.setFontFamily(attrs, font.getFamily());
            StyleConstants.setFontSize(attrs, font.getSize());
            StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
        }
        if ( c != null )
        {
            StyleConstants.setForeground(attrs, c);
        }
        doc.setCharacterAttributes(start, end, attrs, false);
    }

    @Override
    public LogfilesContainer getContainer()
    {
        return null;
    }

    @Override
    public LogfileConfiguration getConfiguration()
    {
        return config;
    }

    @Override
    public JComponent getComponent()
    {
        return splitter;
    }

    @Override
    public void followTailChanged(boolean doFollowTail)
    {
        followTail = doFollowTail;
        if ( followTail )
        {
            scrollToEnd();
            fireContentChanged(false);
        }
    }

    @Override
    public void updateFollowTailCheckbox(boolean doFollowTail)
    {
        container.updateFollowTailCheckbox(doFollowTail, this);
    }

    @Override
    public void addLogfileStateChangedListener(LogfileStateChangedListener listener)
    {
        if ( !this.listener.contains(listener) )
        {
            this.listener.add(listener);
        }
    }

    public void fireContentChanged(boolean hasNew)
    {
        for ( LogfileStateChangedListener list : listener )
        {
            list.contentChanged(hasNew, this);
        }
    }

    public void destroy()
    {
        listener.clear();
        listener = null;

        if ( register != null )
        {
            register.cancel();
            try
            {
                watcher.close();
            }
            catch ( IOException e )
            {
                LogViewer.handleException(e);
            }
        }
    }

    @Override
    public byte[] getBytes()
    {
        return textPane.getText().getBytes();
    }

    @Override
    public String getName()
    {
        return file.getAbsolutePath();
    }

    public String getDisplayName()
    {
        return file.getName();
    }

    @Override
    public String getAbsolutePath()
    {
        return file.getAbsolutePath();
    }

    @Override
    public void dispose()
    {
        // nothing
    }

    private Runnable createReaderThreadRunnable()
    {
        return () -> {
            try
            {
                readFileFully();
                Tailer.create(file, Charset.defaultCharset(), createTailListener(), 500, true, true, 4096);
            }
            catch ( Exception e )
            {
                LogViewer.handleException(e);
            }
        };
    }

    private void clearModel()
    {
        textPane.getHighlighter().removeAllHighlights();
        bookmarkManager.deleteAllBookmarks();
        try
        {
            doc.remove(0, doc.getLength());
        }
        catch ( BadLocationException e )
        {
            LogViewer.handleException(e);
        }
        ((DefaultTableModel) docTable.getModel()).setRowCount(0);
    }

    private void scrollToEnd()
    {
        scrollTo(doc.getLength(), docTable.getRowCount(), false);
    }

    private void readFileFully()
    {
        JLabel waitLabel = new JLabel("Please wait....");
        try
        {
            SwingUtilities.invokeAndWait(() -> {
                clearModel();
                textPane.setDocument(new DefaultStyledDocument());
                textPane.setLayout(new BorderLayout());
                waitLabel.setBackground(new Color(230, 230, 230));
                waitLabel.setHorizontalAlignment(SwingConstants.CENTER);
                waitLabel.setOpaque(true);
                waitLabel.setFont(new Font("Segoe UI, Arial", Font.BOLD, 36));
                textPane.add(waitLabel, BorderLayout.CENTER);
            });
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {

            try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath())))
            {
                stream.forEach(this::processLine);
            }
            catch ( IOException e )
            {
                LogViewer.handleException(e);
            }
            finally
            {
                textPane.remove(waitLabel);
                textPane.setDocument(doc);
                docTable.packAll();

                fireContentChanged(true);
                if ( followTail )
                {
                    scrollToEnd();
                }
            }
        });
    }

    private String nl()
    {
        if ( newLine == null )
        {
            newLine = System.getProperty("line.separator");
        }
        return newLine;
    }

    private TailerListener createTailListener()
    {
        return new TailerListenerAdapter()
        {
            int i = 0;

            @Override
            public void fileRotated()
            {
                try
                {
                    readFileFully();
                }
                catch ( Exception e )
                {
                    LogViewer.handleException(e);
                }
            }

            @Override
            public void handle(String s)
            {
                SwingUtilities.invokeLater(() -> {
                    processLine(s);
                    fireContentChanged(true);
                });
            }

            @Override
            public void handle(Exception e)
            {
                LogViewer.handleException(e);
            }
        };
    }

    private void processLine(final String line)
    {
        try
        {
            DefaultTableModel tableModel = ((DefaultTableModel) docTable.getModel());
            int docLen = doc.getLength();
            final int start = docLen;
            StringBuilder builder = new StringBuilder();
            builder.append(line);
            builder.append(nl());
            String content = builder.toString();

            doc.insertString(docLen, content, null);
            final int end = doc.getLength();
            final HighlightEntry entry = HighlightManager.findHighlightEntry(line);
            if ( entry != null )
            {
                addHighlightToLine(start, entry.getBackgroundColor(), false);
                setStyle(entry, start, end);
                if ( entry.isAddBookmark() )
                {
                    bookmarkManager.addBookmark(new DefaultBookmark(this,
                                                                    start,
                                                                    tableModel.getRowCount() - 1,
                                                                    entry,
                                                                    line));
                }
            }

            LogfileColumn[] columns = LogfileParser.parseLine(columnConfig, line);

            Vector<CellInfos> vector = new Vector<>();
            vector.add(new CellInfos(entry, (tableModel.getRowCount() + 1) + ""));
            for ( LogfileColumn col : columns )
            {
                vector.add(new CellInfos(entry, col.getContent()));
            }
            tableModel.addRow(vector);
        }
        catch ( Exception e )
        {
            LogViewer.handleException(e);
        }
    }

    private int getNewlineBefore(int offset)
    {

        int preOffset = Math.max(0, offset - 100);

        try
        {
            String text = doc.getText(preOffset, offset - preOffset);
            int lastIndex = text.lastIndexOf(nl());
            if ( lastIndex >= 0 )
            {
                return preOffset + lastIndex + 1;
            }
            if ( preOffset == 0 )
            {
                return 0;
            }
            return getNewlineBefore(preOffset);

        }
        catch ( BadLocationException e )
        {
            LogViewer.handleException(e);
        }
        return -1;
    }

    private Object addHighlightToLine(int offset, Color color, boolean isMaster)
    {
        int start = getNewlineBefore(offset);
        if ( start < 0 )
        {
            return null;
        }
        start += 1;

        if ( isMaster )
        {
            selectPainter.setOffset(start);
        }
        try
        {
            return textPane.getHighlighter().addHighlight(offset,
                                                          offset,
                                                          isMaster
                                                                  ? selectPainter
                                                                  : new LinePainter(textPane,
                                                                                    color,
                                                                                    start,
                                                                                    selectPainter));
        }
        catch ( BadLocationException e )
        {
            LogViewer.handleException(e);
        }
        return null;
    }

    private void copySelectedLineToClipboard()
    {
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try
        {
            String text = doc.getText(selectPainter.getOffset(),
                                      Math.min(doc.getLength() - selectPainter.getOffset(), 1000));
            int idxNextNL = text.indexOf(nl()) + 1;
            if ( idxNextNL <= 0 )
            {
                idxNextNL = doc.getLength() - selectPainter.getOffset();
            }

            String theString = doc.getText(getNewlineBefore(selectPainter.getOffset()), idxNextNL);
            if ( theString.startsWith(nl()) )
            {
                theString = theString.substring(nl().length());
            }
            else if ( theString.startsWith("\n") )
            {
                theString = theString.substring(1);
            }
            StringSelection selection = new StringSelection(theString);
            systemClipboard.setContents(selection, selection);
        }
        catch ( BadLocationException e1 )
        {
            LogViewer.handleException(e1);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if ( e != null && (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) )
        {
            copySelectedLineToClipboard();
            return;
        }

        if ( selectHighlightInfo != null )
        {
            textPane.getHighlighter().removeHighlight(selectHighlightInfo);
        }
        selectHighlightInfo = addHighlightToLine(textPane.getCaretPosition(), selectionColor, true);
        textPane.repaint();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column)
    {
        CustomSelectionPaintLabel label = new CustomSelectionPaintLabel();

        CellInfos ci = (CellInfos) value;
        label.setText(ci.content);
        label.isFocused = hasFocus;
        label.isSelected = isSelected;
        Color fgColor = Color.BLACK;
        Color bgColor = Color.WHITE;
        Font font = textPane.getFont();

        if ( ci.entry != null )
        {
            fgColor = ci.entry.getForegroundColor();
            bgColor = ci.entry.getBackgroundColor();
            font = ci.entry.getFont();
        }

        label.setFont(font);
        label.setForeground(fgColor);
        label.setBackground(bgColor);
        label.setOpaque(true);

        label.setWrapStyleWord(false);
        label.setLineWrap(lineWrap);

        FontMetrics fontMetrics = label.getFontMetrics(label.getFont());

        int newHeight = fontMetrics.getHeight() + 3;
        if ( label.getLineWrap() )
        {
            int fontHeight = fontMetrics.getHeight();

            Rectangle2D stringBounds = fontMetrics.getStringBounds(label.getText(), label.getGraphics());
            int textWidth = (int) ((stringBounds.getWidth() + 10) * 1.05);
            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            newHeight = (textWidth / colWidth + 1) * (fontHeight + 1);
            if ( column > 0 )
            {
                newHeight = Math.max(table.getRowHeight(row), newHeight);
            }
        }
        table.setRowHeight(row, newHeight);

        label.setToolTipText(label.getText());

        return label;
    }

    private class CustomSelectionPaintLabel // NOSONAR
            extends JTextArea
    {

        boolean isSelected = false;
        boolean isFocused = false;

        @Override
        public void paint(Graphics g)
        {
            super.paint(g);
            if ( isSelected )
            {
                int middleBG = (getBackground().getRed() + getBackground().getGreen() + getBackground().getBlue()) / 3;
                if ( middleBG < 80 )
                {
                    g.setColor(new Color(160, 210, 255, 160));
                }
                else
                {
                    g.setColor(new Color(63, 72, 255, 160));
                }

                g.fillRect(0, 0, getSize().width, getSize().height);
            }
        }
    }

    private class CellInfos
    {

        private HighlightEntry entry;
        private String content;

        public CellInfos(HighlightEntry entry, String content)
        {
            this.entry = entry;
            this.content = content;
        }

        public HighlightEntry getEntry()
        {
            return entry;
        }

        public String getContent()
        {
            return content;
        }

        @Override
        public String toString()
        {
            return content;
        }
    }

    private class WrapEditorKit
            extends StyledEditorKit
    {
        transient ViewFactory defaultFactory = new WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory()
        {
            return defaultFactory;
        }

    }

    private class WrapColumnFactory
            implements ViewFactory
    {
        public View create(Element elem)
        {
            String kind = elem.getName();
            if ( kind != null )
            {
                if ( kind.equals(AbstractDocument.ContentElementName) )
                {
                    return new WrapLabelView(elem);
                }
                else if ( kind.equals(AbstractDocument.ParagraphElementName) )
                {
                    return new ParagraphView(elem);
                }
                else if ( kind.equals(AbstractDocument.SectionElementName) )
                {
                    return new BoxView(elem, View.Y_AXIS);
                }
                else if ( kind.equals(StyleConstants.ComponentElementName) )
                {
                    return new ComponentView(elem);
                }
                else if ( kind.equals(StyleConstants.IconElementName) )
                {
                    return new IconView(elem);
                }
            }

            return new LabelView(elem);
        }
    }

    private class WrapLabelView
            extends LabelView
    {
        public WrapLabelView(Element elem)
        {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis)
        {
            switch ( axis )
            {
                case View.X_AXIS:
                    return minLabelSpan;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

    }
}
