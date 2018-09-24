package com.betera.logviewer.ui.fileviewer;

import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.file.LogfileConfiguration;
import com.betera.logviewer.file.LogfileStateChangedListener;
import com.betera.logviewer.file.LogfilesContainer;
import com.betera.logviewer.file.highlight.HighlightEntry;
import com.betera.logviewer.file.highlight.HighlightManager;
import com.betera.logviewer.ui.bookmark.BookmarkManager;
import com.betera.logviewer.ui.bookmark.DefaultBookmark;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
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

public class JTextPaneLogfile
        implements Logfile, Runnable, MouseListener
{

    Action selectLine;
    private File file;
    private JPanel panel;
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
    private LogfilesContainer container;
    private AdjustmentListener scrollListener = new AdjustmentListener()
    {

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e)
        {
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
        }
    };
    private List<LogfileStateChangedListener> listener;
    private WatchService watcher;
    private WatchKey register;

    public JTextPaneLogfile(LogfilesContainer container, File aFile, LogfileConfiguration config)
    {
        selectionColor = new Color(51, 153, 255, 255);

        listener = new ArrayList<>();

        file = aFile;
        disposed = false;
        this.container = container;
        this.config = config;

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        textPane = new JTextPane();
        textPane.setBorder(BorderFactory.createEtchedBorder());
        textPane.setEditable(false);
        textPane.addMouseListener(this);
        textPane.setEditorKit(new WrapEditorKit());
        textPane.setBackground(HighlightManager.getDefaultEntry().getBackgroundColor());
        doc = textPane.getStyledDocument();

        DefaultHighlighter highlighter = new DefaultHighlighter();
        highlighter.setDrawsLayeredHighlights(false);
        textPane.setHighlighter(highlighter);
        selectPainter = new LinePainter(textPane, selectionColor, 0, null);

        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(textPane);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(scrollListener);

        panel.add(scrollPane, BorderLayout.CENTER);

        followTail = true;

        readFile();

        readerThread = new Thread(this);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    @Override
    public String toString()
    {
        return getDisplayName();
    }

    @Override
    public void scrollTo(int offset)
    {
        textPane.setCaretPosition(offset);
        mouseClicked(null);
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
        if ( bgColor != null )
        {
//            StyleConstants.setBackground(attrs, bgColor);
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
        return panel;
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

    public void run()
    {
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
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    doc.remove(0, doc.getLength());
                                    oldLength = 0;
                                }
                                catch ( BadLocationException e )
                                {
                                    e.printStackTrace();
                                }
                            }
                        });
                        fileChanged = true;
                    }
                    else if ( kind.equals(StandardWatchEventKinds.ENTRY_CREATE) )
                    {
                        SwingUtilities.invokeLater(() -> {
                            try
                            {
                                doc.remove(0, doc.getLength());
                                oldLength = 0;
                                readFile();
                            }
                            catch ( BadLocationException e )
                            {
                                e.printStackTrace();
                            }
                        });
                        fileChanged = true;
                    }
                    else if ( kind.equals(StandardWatchEventKinds.ENTRY_MODIFY) )
                    {
                        SwingUtilities.invokeLater(() -> {
                            try
                            {
                                long newLength = file.length();
                                if ( newLength < oldLength )
                                {
                                    doc.remove(0, doc.getLength());
                                    oldLength = 0;
                                }
                                readFile();
                            }
                            catch ( BadLocationException e )
                            {
                                e.printStackTrace();
                            }
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

    private void scrollToEnd()
    {
        if ( scrollPane.isVisible() )
        {
            JScrollBar vsb = scrollPane.getVerticalScrollBar();
            vsb.setValue(vsb.getMaximum() + vsb.getModel().getExtent());
            textPane.setCaretPosition(doc.getLength());
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
                final String finalLine = line;
                processLine(finalLine);
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
            scrollPane.revalidate();
            scrollPane.repaint();
            fireContentChanged(true);
            if ( followTail )
            {
                scrollToEnd();
            }
            scrollListenerEnabled = true;
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

    public void processLine(String line)
    {
        try
        {
            int start = doc.getLength();
            doc.insertString(doc.getLength(), line + nl(), null);
            int end = doc.getLength();

            HighlightEntry entry = HighlightManager.findHighlightEntry(line);
            if ( entry == null )
            {
                entry = HighlightManager.getDefaultEntry();
            }

            addHighlightToLine(start, entry.getBackgroundColor(), false);
            setStyle(entry, start, end);
            if ( entry.isAddBookmark() )
            {
                BookmarkManager.addBookmark(new DefaultBookmark(this, start, entry, line));
            }
        }
        catch ( BadLocationException e )
        {
            e.printStackTrace();
        }
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

    class WrapEditorKit
            extends StyledEditorKit
    {
        ViewFactory defaultFactory = new WrapColumnFactory();

        public ViewFactory getViewFactory()
        {
            return defaultFactory;
        }

    }

    class WrapColumnFactory
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

            // default to text display
            return new LabelView(elem);
        }
    }

    class WrapLabelView
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
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

    }
}
