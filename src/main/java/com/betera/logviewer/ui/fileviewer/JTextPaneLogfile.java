package com.betera.logviewer.ui.fileviewer;

import com.betera.logviewer.Debug;
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultHighlighter;
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
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;

public class JTextPaneLogfile
        implements Logfile, MouseListener, TableCellRenderer
{

    private Action selectLine;
    private File file;
    private JScrollPane scrollPane;
    private JTextPane textPane;
    private LogfileConfiguration config;
    private Thread readerThread;
    private boolean disposed;
    private String newLine;
    private StyledDocument doc;
    private boolean scrollListenerEnabled = true;
    private int lastScrollPosition;

    private LinePainter selectPainter = null;
    private Object selectHighlightInfo;
    private boolean followTail;
    private Color selectionColor;
    private long oldLength;
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

    public JTextPaneLogfile(LogfilesContainer container, File aFile, LogfileConfiguration config)
    {
        selectionColor = new Color(51, 153, 255, 255);

        listener = new ArrayList<>();

        file = aFile;
        disposed = false;
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

    private AdjustmentListener createScrollListener()
    {
        return new AdjustmentListener()
        {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                Debug.start("Adjust Scrollbar");
                JScrollBar vsb = scrollPane.getVerticalScrollBar();

                int value = vsb.getValue() + vsb.getModel().getExtent();
                int max = vsb.getMaximum();

                if ( value < lastScrollPosition )
                {
                    followTail = false;
                }
                else if ( value >= max )
                {
                    followTail = true;
                }
                updateFollowTailCheckbox(followTail);

                lastScrollPosition = value;
                Debug.end();
            }
        };
    }

    private void initReaderThread()
    {
        readerThread = new Thread(createReaderThreadRunnable());
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void initSplitterAndScrollPane()
    {
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(textPane);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(createScrollListener());

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
        createToolWindowButton(toolsToolbar, taskContainer, bookmarkManager.getComponent(), "Bookmarks");
        createToolWindowButton(toolsToolbar, taskContainer, new JPanel(), "Test");
        toolsToolbar.setFloatable(false);
        toolsToolbar.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        taskContainer.add(bookmarkManager.getComponent());
    }

    private void initTable()
    {
        docTable = new JXTable();
        docTable.setSortable(false);
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

    private void initTextPane()
    {
        textPane = new JTextPane();
        textPane.setBorder(BorderFactory.createEtchedBorder());
        textPane.setEditable(false);
        textPane.addMouseListener(this);
        textPane.setEditorKit(new WrapEditorKit());
        textPane.setBackground(HighlightManager.getDefaultEntry().getBackgroundColor());
        doc = textPane.getStyledDocument();
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textPane.setFont(new Font("Consolas", Font.PLAIN, 18));

    }

    private JToolBar createLogfileToolbar()
    {
        final ImageIcon tableIcon = new ImageIcon("./images/table.png");
        final ImageIcon textPaneIcon = new ImageIcon("./images/textPane.png");

        JToolBar logRootToolBar = new JToolBar();
        logRootToolBar.setLayout(new BorderLayout());
        JToolBar leftTB = new JToolBar();
        leftTB.setFloatable(false);
        JToolBar rightTB = new JToolBar();
        rightTB.setFloatable(false);
        logRootToolBar.add(leftTB, BorderLayout.CENTER);
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
        leftTB.add(new AbstractAction("Toggle view", new ImageIcon("./images/table.png"))
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Component comp = scrollPane.getViewport().getView();
                if ( comp instanceof JXTable )
                {
                    scrollPane.setViewportView(textPane);
                    putValue(Action.SMALL_ICON, tableIcon);
                }
                else
                {
                    docTable.packAll();
                    scrollPane.setViewportView(docTable);
                    putValue(Action.SMALL_ICON, textPaneIcon);
                }
            }
        });
        Icon lineWrapOnIcon = new ImageIcon("./images/arrowDown.png");
        Icon lineWrapOffIcon = new ImageIcon("./images/arrowUp.png");
        leftTB.add(new AbstractAction("Toggle Line-Wrap", lineWrapOnIcon)
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
                docTable.repaint();
            }
        });
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
        boolean[] hidden = new boolean[cols.length];
        cols[0] = "Line";
        for ( int i = 0; i < columnConfig.getEntries().length; i++ )
        {
            cols[i + 1] = columnConfig.getEntries()[i].getColumnName();
        }

        ((DefaultTableModel) docTable.getModel()).setColumnIdentifiers(cols);
        DefaultTableColumnModelExt mdl = (DefaultTableColumnModelExt) docTable.getColumnModel();

        List<TableColumn> columns = mdl.getColumns(true);
        for ( TableColumn column : columns )
        {
            String identifier = (String) column.getIdentifier();
            for ( LogfileColumnConfigEntry entry : columnConfig.getEntries() )
            {
                if ( entry.getColumnName().equals(identifier) && entry.isInitiallyHidden() )
                {
                    mdl.getColumnExt(identifier).setVisible(false);
                    break;
                }
            }
        }
    }

    private void createToolWindowButton(JToolBar toolBar, JPanel container, JComponent aComp, String aTitle)
    {

        JToggleButton btn = new JVerticalButton();
        btn.setText(aTitle);
        aComp.setBorder(new EmptyBorder(1, 1, 1, 1));
        btn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                container.removeAll();
                container.add(aComp, BorderLayout.CENTER);
                container.revalidate();
                container.repaint();
            }
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
    public void scrollTo(int offset, int row)
    {
        textPane.setCaretPosition(offset);
        docTable.getSelectionModel().setSelectionInterval(row + 1, row + 1);
        scrollToVisible(row, 0);
        mouseClicked(null);
    }

    private void scrollToVisible(int rowIndex, int vColIndex)
    {
        JTable table = docTable;
        if ( !(table.getParent() instanceof JViewport) )
        {
            return;
        }
        if ( table.getRowCount() < 1 )
        {
            return;
        }
        JViewport viewport = (JViewport) table.getParent();
        Dimension dim = viewport.getExtentSize();
        Dimension dimOne = new Dimension(0, 0);

        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
        Rectangle rectOne;
        if ( rowIndex + 1 < table.getRowCount() )
        {
            if ( vColIndex + 1 < table.getColumnCount() )
            {
                vColIndex++;
            }
            rectOne = table.getCellRect(rowIndex + 1, vColIndex, true);
            dimOne.width = rectOne.x - rect.x;
            dimOne.height = rectOne.y - rect.y;
        }

        rect.setLocation(rect.x + dim.width - dimOne.width, rect.y + dim.height - dimOne.height);
        table.scrollRectToVisible(rect);
    }

    private void setStyle(HighlightEntry entry, int start, int end)
    {
        Font font = entry.getFont();
        Color c = entry.getForegroundColor();
        Color bgColor = entry.getBackgroundColor();

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

        disposed = true;

        if ( register != null )
        {
            register.cancel();
            try
            {
                watcher.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
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
        disposed = true;
    }

    private Runnable createReaderThreadRunnable()
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                readFile();
                SwingUtilities.invokeLater(() -> docTable.packAll());

                try
                {
                    watcher = FileSystems.getDefault().newWatchService();
                    register = file.getParentFile().toPath().register(watcher,
                                                                      StandardWatchEventKinds.ENTRY_MODIFY,
                                                                      StandardWatchEventKinds.ENTRY_DELETE,
                                                                      StandardWatchEventKinds.ENTRY_CREATE);

                    while ( !disposed )
                    {
                        WatchKey poll = watcher.take();

                        boolean fileChanged = false;
                        for ( WatchEvent event : poll.pollEvents() )
                        {
                            Kind kind = event.kind();
                            if ( kind.equals(StandardWatchEventKinds.OVERFLOW) )
                            {
                                continue;
                            }
                            Path path = (Path) event.context();
                            if ( !path.toFile().getName().equals(file.getName()) )
                            {
                                continue;
                            }

                            if ( kind.equals(StandardWatchEventKinds.ENTRY_DELETE) )
                            {
                                SwingUtilities.invokeLater(() -> {
                                    clearModel();
                                    oldLength = 0;
                                });
                                fileChanged = true;
                            }
                            else if ( kind.equals(StandardWatchEventKinds.ENTRY_CREATE) )
                            {
                                SwingUtilities.invokeLater(() -> {
                                    clearModel();
                                    readFile();
                                });
                                fileChanged = true;
                            }
                            else if ( kind.equals(StandardWatchEventKinds.ENTRY_MODIFY) )
                            {
                                SwingUtilities.invokeLater(() -> {
                                    long newLength = file.length();
                                    if ( newLength < oldLength )
                                    {
                                        clearModel();
                                        oldLength = 0;
                                    }
                                    readFile();
                                });
                                fileChanged = true;
                            }
                        }

                        poll.reset();
                    }
                }
                catch ( IOException | InterruptedException e )
                {
                    e.printStackTrace();
                }
            }
        };
    }

    private void clearModel()
    {
        try
        {
            doc.remove(0, doc.getLength());
        }
        catch ( BadLocationException e )
        {
            e.printStackTrace();
        }
        ((DefaultTableModel) docTable.getModel()).setRowCount(0);
    }

    private void scrollToEnd()
    {
        if ( scrollPane.isVisible() )
        {
            JScrollBar vsb = scrollPane.getVerticalScrollBar();
            vsb.setValue(vsb.getMaximum() + vsb.getModel().getExtent());
            textPane.setCaretPosition(doc.getLength());
            docTable.scrollRowToVisible(docTable.getRowCount());
        }
    }

    private void readFile()
    {
        scrollListenerEnabled = false;
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(file)))
        {
            lnr.skip(oldLength);
            String line = lnr.readLine();
            while ( line != null )
            {
                processLine(line);
                line = lnr.readLine();
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            oldLength = file.length();
            SwingUtilities.invokeLater(() -> {
                fireContentChanged(true);
                if ( followTail )
                {
                    scrollToEnd();
                }
                scrollListenerEnabled = true;
            });
        }
    }

    private String nl()
    {
        if ( newLine == null )
        {
            newLine = System.getProperty("line.separator");
        }
        return newLine;
    }

    private void processLine(String line)
    {
        SwingUtilities.invokeLater(() -> {
            try
            {
                Debug.start("processLine");
                DefaultTableModel tableModel = ((DefaultTableModel) docTable.getModel());
                final int start = doc.getLength();
                doc.insertString(doc.getLength(), line + nl(), null);
                final int end = start + (line + nl()).length();
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
                vector.add(new CellInfos(null, (tableModel.getRowCount() + 1) + ""));
                for ( LogfileColumn col : columns )
                {
                    vector.add(new CellInfos(entry, col.getContent()));
                }
                tableModel.addRow(vector);
                Debug.end();
            }
            catch ( BadLocationException e )
            {
                e.printStackTrace();
            }
        });
    }

    private int getNewlineBefore(int offset)
    {

        int preOffset = Math.max(0, offset - 100);

        try
        {
            String text = textPane.getText(preOffset, offset - preOffset);
            int lastIndex = text.lastIndexOf(System.getProperty("line.separator"));
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
            e.printStackTrace();
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

        selectPainter.setOffset(start);

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
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if ( selectHighlightInfo != null )
        {
            textPane.getHighlighter().removeHighlight(selectHighlightInfo);
        }
        selectHighlightInfo = addHighlightToLine(textPane.getCaretPosition(), selectionColor, true);
        textPane.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {

    }

    @Override
    public void mouseReleased(MouseEvent e)
    {

    }

    @Override
    public void mouseEntered(MouseEvent e)
    {

    }

    @Override
    public void mouseExited(MouseEvent e)
    {

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
            int textLength = label.getText().length();

            Rectangle2D stringBounds = fontMetrics.getStringBounds(label.getText(), label.getGraphics());
            int textWidth = (int) ((stringBounds.getWidth() + 10) * 1.05);
            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            newHeight = (textWidth / colWidth + 1) * (fontHeight + 1);
            newHeight = Math.max(table.getRowHeight(row), newHeight);
        }
        table.setRowHeight(row, newHeight);

        label.setToolTipText(label.getText());

        return label;
    }

    private class CustomSelectionPaintLabel
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
        ViewFactory defaultFactory = new WrapColumnFactory();

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
