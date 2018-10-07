package com.betera.logviewer;

import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.file.LogfilesContainer;
import com.betera.logviewer.file.TabBasedLogfilesContainer;
import com.betera.logviewer.file.column.LogfileParser;
import com.betera.logviewer.file.highlight.HighlightEntry;
import com.betera.logviewer.file.highlight.HighlightManager;
import com.betera.logviewer.ui.action.OpenFileAction;
import com.betera.logviewer.ui.action.RunMavenAction;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LogViewer
{

    public static final int BOOKMARK_MAX_TITLE_SIZE = 40;

    public static final String PROP_LOGFILES = "pref.openLogfiles";
    private static final Object PROP_FOLLOW_TAIL = "pref.followTail";

    private JFrame mainFrame;

    private JMenuBar menuBar;

    private JMenu fileMenu;

    private JMenuItem openFileMenuItem;

    private LogfilesContainer logContainer;

    private JCheckBox followTailCheckbox;

    private JPanel content;

    public LogViewer()
            throws
            ClassNotFoundException,
            UnsupportedLookAndFeelException,
            InstantiationException,
            IllegalAccessException,
            IOException
    {
        init();
    }

    public static void main(String[] args)
            throws
            ClassNotFoundException,
            UnsupportedLookAndFeelException,
            InstantiationException,
            IllegalAccessException,
            IOException
    {
        LogViewer logViewer = new LogViewer();
    }

    private void init()
            throws
            ClassNotFoundException,
            UnsupportedLookAndFeelException,
            InstantiationException,
            IllegalAccessException,
            IOException
    {

        readHighlightConfig();
        LogfileParser.readColumnFormatterConfig();

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        mainFrame = new JFrame();
        ImageIcon icon = new ImageIcon("./images/logviewer.png");
        mainFrame.setIconImage(icon.getImage());
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                try
                {
                    savePreferences();
                    Debug.printStatistics();
                }
                catch ( IOException e1 )
                {
                    e1.printStackTrace();
                }
            }
        });
        logContainer = createLogfilesContainer();

        content = new JPanel();
        content.setLayout(new BorderLayout());

        content.add(logContainer.getComponent(), BorderLayout.CENTER);
        content.add(createToolbar(), BorderLayout.NORTH);

        mainFrame.getContentPane().add(content, BorderLayout.CENTER);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.setVisible(true);

        loadPreferences();
    }

    private void loadLogfile(String path)
    {
        logContainer.addFile(new File(path));
    }

    private void loadPreferences()
            throws IOException
    {
        Properties pref = new Properties();
        pref.load(new FileReader("preferences.config"));

        int i = 0;
        String logfilePath = pref.getProperty(PROP_LOGFILES + "." + i);
        while ( logfilePath != null )
        {
            loadLogfile(logfilePath);
            logfilePath = pref.getProperty(PROP_LOGFILES + "." + (++i));
        }

        boolean followTail = Boolean.TRUE.equals(pref.get(PROP_FOLLOW_TAIL) + "");
        followTailCheckbox.setSelected(followTail);
        logContainer.updateFollowTailCheckbox(followTail, null);
    }

    private void savePreferences()
            throws IOException
    {
        Properties pref = new Properties();

        int i = 0;
        for ( Logfile logfile : logContainer.getOpenLogfiles() )
        {
            pref.put(PROP_LOGFILES + "." + i, logfile.getAbsolutePath());
            i++;
        }

        pref.put(PROP_FOLLOW_TAIL, getFollowTailCheckbox().isSelected() + "");
        pref.store(new FileWriter("preferences.config"), "Auto-generated by LogViewer");
    }

    public JCheckBox getFollowTailCheckbox()
    {
        return followTailCheckbox;
    }

    private JComponent createMavenToolbar()
    {
        JToolBar tb = new JToolBar();
        tb.setBorderPainted(true);
        tb.setBorder(BorderFactory.createTitledBorder("Maven"));
        tb.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
        tb.setFloatable(false);
        JComboBox projCB = new JComboBox();
        projCB.addItem("PRODMGMT");
        projCB.addItem("PLATFORM");
        tb.add(projCB);

        JComboBox goalCB = new JComboBox();
        goalCB.addItem("test");
        goalCB.addItem("compile");
        goalCB.addItem("install");
        tb.add(goalCB);

        tb.add(new JCheckBox("Skip Tests"));
        tb.add(new JCheckBox("Profile:"));
        tb.add(new JTextField("gwt-debug-firefox"));
        tb.add(new JLabel("Deploy to:"));

        JComboBox deployCB = new JComboBox();
        deployCB.addItem("EMES");
        deployCB.addItem("test732");
        tb.add(deployCB);

        tb.add(new RunMavenAction());

        return tb;
    }

    private JToolBar createToolbar()
    {
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
        toolbar.setSize(new Dimension(200, 48));
        toolbar.setFloatable(false);
        toolbar.add(new OpenFileAction(logContainer));

        followTailCheckbox = new JCheckBox("Follow Tail");
        followTailCheckbox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean doFollowTail = e.getStateChange() == ItemEvent.SELECTED;
                logContainer.fireFollowTailChanged(doFollowTail, null);
            }
        });
        toolbar.add(createSeparator());
        toolbar.add(followTailCheckbox);
        toolbar.add(createMavenToolbar());
        return toolbar;
    }

    private JSeparator createSeparator()
    {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 16));
        return sep;
    }

    private void readHighlightConfig()
            throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("highlight.config"))))
        {
            String line = reader.readLine();
            while ( line != null )
            {
                if ( line.startsWith("[Highlight ") )
                {
                    String text = line.substring(line.indexOf(' ') + 1, line.length() - 1);
                    boolean isDefault = text.equals("###DEFAULT###");
                    String fontName = reader.readLine();
                    line = reader.readLine();
                    String[] stuff = line.split(" ");
                    String sFontType = stuff[0];
                    String sFontSize = stuff[1];
                    String foregroundColor = stuff[2];
                    String backgroundColor = stuff[3];
                    String sAddBookmark = "0";
                    if ( stuff.length >= 5 )
                    {
                        sAddBookmark = stuff[4];
                    }

                    int fontType = Integer.valueOf(sFontType);
                    int fontSize = Integer.valueOf(sFontSize);
                    javafx.scene.paint.Color fgColor = javafx.scene.paint.Color.valueOf(foregroundColor);
                    javafx.scene.paint.Color bgColor = javafx.scene.paint.Color.valueOf(backgroundColor);
                    boolean addBookmark = "1".equals(sAddBookmark);

                    if ( !isDefault )
                    {
                        fontSize = HighlightManager.getDefaultEntry().getFont().getSize() + fontSize;
                    }

                    Font font = new Font(fontName, fontType, fontSize);
                    HighlightEntry entry = new HighlightEntry(text,
                                                              font,
                                                              new java.awt.Color((float) fgColor.getRed(),
                                                                                 (float) fgColor.getGreen(),
                                                                                 (float) fgColor.getBlue()),
                                                              new java.awt.Color((float) bgColor.getRed(),
                                                                                 (float) bgColor.getGreen(),
                                                                                 (float) bgColor.getBlue()),
                                                              addBookmark);

                    if ( isDefault )
                    {
                        HighlightManager.registerDefault(entry);
                    }
                    else
                    {
                        HighlightManager.registerHighlight(entry);
                    }
                }
                line = reader.readLine();
            }

        }

    }

    private LogfilesContainer createLogfilesContainer()
    {
        return new TabBasedLogfilesContainer(this);
    }

    private JMenuBar createMenuBar()
    {
        menuBar = new JMenuBar();

        fileMenu = new JMenu();
        fileMenu.setText("File");

        openFileMenuItem = new JMenuItem();
        openFileMenuItem.setAction(new OpenFileAction(logContainer));

        fileMenu.add(openFileMenuItem);

        menuBar.add(fileMenu);

        return menuBar;
    }

}
