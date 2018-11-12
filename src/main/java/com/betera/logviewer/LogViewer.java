package com.betera.logviewer;

import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.file.LogfilesContainer;
import com.betera.logviewer.file.TabBasedLogfilesContainer;
import com.betera.logviewer.file.column.LogfileParser;
import com.betera.logviewer.file.highlight.HighlightManager;
import com.betera.logviewer.ui.CollapsiblePanel;
import com.betera.logviewer.ui.JFontChooser;
import com.betera.logviewer.ui.LogViewerLookAndFeel;
import com.betera.logviewer.ui.action.AboutAction;
import com.betera.logviewer.ui.action.EditAction;
import com.betera.logviewer.ui.action.OpenFileAction;
import com.betera.logviewer.ui.maven.MavenConfigManager;
import com.betera.logviewer.ui.maven.MavenManager;
import com.jtattoo.plaf.AbstractLookAndFeel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

public class LogViewer
{

    public static final int BOOKMARK_MAX_TITLE_SIZE = 40;

    public static final String PROP_MAVEN_LOGFILE = "pref.mavenLogfile";
    public static final String PROP_LOGFILES = "pref.openLogfiles";
    public static final String VERSION = "1.0";
    private static final Object PROP_FOLLOW_TAIL = "pref.followTail";
    private static final Object PROP_FOCUS_ACTIVE = "pref.focusActive";
    private static final int MAX_RECENT_FILE_SIZE = 10;
    public static String mavenLogfile;
    private static JFrame mainFrame;
    private static GlassPane mfGlassPane;
    private static AbstractLookAndFeel laf;
    private LogfilesContainer logContainer;
    private JCheckBox followTailCheckbox;
    private JCheckBox focusActiveCheckbox;
    private JPanel content;
    private List<String> recentFiles;

    public LogViewer()

    {
        try
        {
            init();
        }
        catch ( Exception e )
        {
            handleException(e);
        }
    }

    public static void main(String[] args)
    {
        new LogViewer();
    }

    public static void handleException(Exception exc, String msg)
    {
        StringWriter sOut = new StringWriter();
        PrintWriter out = new PrintWriter(sOut);
        if ( msg != null )
        {
            out.println(msg);
        }
        exc.printStackTrace(out); // NOSONAR
        JOptionPane.showMessageDialog(null, "ERROR: " + sOut.toString());
    }

    public static void handleException(Exception exc)
    {
        handleException(exc, null);
    }

    public static JFrame getMainFrame()
    {
        return mainFrame;
    }

    public static GlassPane getGlassPane()
    {
        return mfGlassPane;
    }

    public JCheckBox getFocusActiveCheckbox()
    {
        return focusActiveCheckbox;
    }

    public void setFocusActiveCheckbox(JCheckBox focusActiveCheckbox)
    {
        this.focusActiveCheckbox = focusActiveCheckbox;
    }

    public List<String> getRecentFiles()
    {
        if ( recentFiles == null )
        {
            recentFiles = new ArrayList<>();
        }
        return recentFiles;
    }

    private void init()
            throws
            ClassNotFoundException,
            UnsupportedLookAndFeelException,
            InstantiationException,
            IllegalAccessException,
            IOException
    {

        laf = new LogViewerLookAndFeel();
        UIManager.setLookAndFeel(laf);
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        HighlightManager.getInstance().readHighlightConfig();
        LogfileParser.getInstance().readColumnFormatterConfig();

        mainFrame = new JFrame("LogViewer v" + VERSION);
        mfGlassPane = new GlassPane();
        mainFrame.setGlassPane(mfGlassPane);

        mainFrame.setIconImage(Icons.logViewerIcon.getImage());
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                try
                {
                    savePreferences();
                    MavenConfigManager.getInstance().saveMavenConfig();
                    HighlightManager.getInstance().saveHighlightConfig();
                    LogfileParser.getInstance().saveConfig();
                }
                catch ( IOException e1 )
                {
                    LogViewer.handleException(e1);
                }
            }
        });
        logContainer = createLogfilesContainer();

        content = new JPanel();
        content.setLayout(new BorderLayout());

        content.add(logContainer.getComponent(), BorderLayout.CENTER);
        content.add(createToolbar(), BorderLayout.NORTH);

        mainFrame.setJMenuBar(createMenuBar());
        mainFrame.getContentPane().add(content, BorderLayout.CENTER);
        mainFrame.setSize(new Dimension(1024, 768));
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        mainFrame.setVisible(true);

        loadPreferences();
    }

    public void addRecentFile(String path)
    {
        while ( getRecentFiles().size() >= MAX_RECENT_FILE_SIZE )
        {
            getRecentFiles().remove(0);
        }

        if ( !getRecentFiles().contains(path) )
        {
            getRecentFiles().add(path);
            updateRecentFilesMenu();
        }
    }

    private void updateRecentFilesMenu()
    {
        JMenu menu = mainFrame.getJMenuBar().getMenu(0);
        int insertPoint = 1;
        for ( int i = 1; i < menu.getItemCount(); i++ )
        {
            if ( menu.getItem(i) == null )
            {
                insertPoint = i;
                break;
            }
        }

        for ( int i = insertPoint - 1; i >= 1; i-- )
        {
            menu.remove(i);
        }

        for ( int i = 0; i < getRecentFiles().size(); i++ )
        {
            final String file = getRecentFiles().get(i);
            menu.insert(new JMenuItem(new AbstractAction(file)
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    logContainer.addFile(new File(file));
                }
            }), 1);
        }
    }

    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new OpenFileAction(this, logContainer));
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(new AbstractAction("Exit")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                mainFrame.dispose();
            }
        }));

        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Options");
        editMenu.add(new EditAction("Edit Highlighting", HighlightManager.getInstance()));
        editMenu.add(new EditAction("Edit Maven Settings", MavenConfigManager.getInstance()));
        editMenu.add(new EditAction("Edit Column Settings", LogfileParser.getInstance()));
        menuBar.add(editMenu);

        JMenu aboutMenu = new JMenu("About");
        aboutMenu.add(new JMenuItem(new AboutAction()));
        menuBar.add(aboutMenu);

        return menuBar;
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

        String recentFilesCSV = pref.getProperty("recentFiles", "");
        for ( String recentFile : recentFilesCSV.split(",") )
        {
            if ( !recentFile.isEmpty() && new File(recentFile).exists() && !getRecentFiles().contains(recentFile) )
            {
                getRecentFiles().add(recentFile);
            }
        }

        updateRecentFilesMenu();
        String mvnLf = (String) pref.get(PROP_MAVEN_LOGFILE);
        if ( mvnLf != null )
        {
            mavenLogfile = mvnLf;
        }
        boolean followTail = "true".equalsIgnoreCase(pref.get(PROP_FOLLOW_TAIL) + "");
        followTailCheckbox.setSelected(followTail);
        boolean focusActive = "true".equalsIgnoreCase(pref.get(PROP_FOCUS_ACTIVE) + "");
        focusActiveCheckbox.setSelected(focusActive);
        logContainer.updateFollowTailCheckbox(followTail, null);

        MavenManager.getInstance().init();

        int i = 0;
        String logfilePath = pref.getProperty(PROP_LOGFILES + "." + i);
        while ( logfilePath != null )
        {
            loadLogfile(logfilePath);
            logfilePath = pref.getProperty(PROP_LOGFILES + "." + (++i));
        }

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

        String recFilesProp = "";
        for ( String af : getRecentFiles() )
        {
            recFilesProp += "," + af;
        }
        if ( recFilesProp.length() > 0 )
        {
            recFilesProp = recFilesProp.substring(1);
        }

        pref.put("recentFiles", recFilesProp);

        if ( mavenLogfile != null )
        {
            pref.put(PROP_MAVEN_LOGFILE, mavenLogfile);
        }
        pref.put(PROP_FOLLOW_TAIL, getFollowTailCheckbox().isSelected() + "");
        pref.put(PROP_FOCUS_ACTIVE, getFocusActiveCheckbox().isSelected() + "");
        pref.store(new FileWriter("preferences.config"), "Auto-generated by LogViewer");
    }

    public JCheckBox getFollowTailCheckbox()
    {
        return followTailCheckbox;
    }

    private JComponent createMavenToolbar()
    {
        JToolBar tb = new JToolBar();
        tb.setMinimumSize(new Dimension(24, 32));

        tb.setLayout(new FlowLayout(FlowLayout.LEADING, 8, 2));
        tb.setFloatable(false);
//        tb.setBorderPainted(true);
//        tb.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), "Maven"));
        tb.add(MavenManager.getInstance().getProjectComboBox());
        tb.add(createSeparator());
        tb.add(MavenManager.getInstance().getDoCleanCheckBox());
        tb.add(new JLabel("Goal:"));
        tb.add(MavenManager.getInstance().getGoalComboBox());
        tb.add(createSeparator());
        tb.add(MavenManager.getInstance().getForceUpdateComboBox());
        tb.add(MavenManager.getInstance().getSkipTestsCheckBox());
        tb.add(MavenManager.getInstance().getUseProfileCheckBox());
        tb.add(MavenManager.getInstance().getProfileTextField());
        tb.add(createSeparator());
        tb.add(new JLabel("Deploy to:"));
        tb.add(MavenManager.getInstance().getDeploymentComboBox());
        tb.add(createSeparator());

        tb.add(MavenManager.getInstance().getAction(logContainer));

        return new CollapsiblePanel(tb, "Maven", true);
    }

    private JComponent createStandardToolbar()
    {
        JToolBar toolbar = new JToolBar();
        toolbar.setBorderPainted(true);
        toolbar.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), "Standard"));
        toolbar.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
        toolbar.setSize(new Dimension(200, 48));
        toolbar.setFloatable(false);
        toolbar.add(new OpenFileAction(this, logContainer));

        followTailCheckbox = new JCheckBox("");
        followTailCheckbox.setToolTipText("Follow tail");
        followTailCheckbox.setIcon(Icons.tailIcon);
        followTailCheckbox.setSelectedIcon(Icons.tailCheckedIcon);
        followTailCheckbox.addItemListener(e -> {
            boolean doFollowTail = e.getStateChange() == ItemEvent.SELECTED;
            logContainer.fireFollowTailChanged(doFollowTail, null);

        });

        focusActiveCheckbox = new JCheckBox("");
        focusActiveCheckbox.setToolTipText("Focus active window");
        focusActiveCheckbox.setIcon(Icons.focusIcon);
        focusActiveCheckbox.setSelectedIcon(Icons.focusCheckedIcon);

        toolbar.add(followTailCheckbox);
        toolbar.add(focusActiveCheckbox);

        toolbar.add(new AbstractAction("Font", Icons.fontIcon)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

                JFontChooser fc = new JFontChooser();
                fc.setPreferredSize(new Dimension(600, 400));
                fc.setSelectedFont(HighlightManager.getInstance().getDefaultEntry().getFont());
                int ret = fc.showDialog(LogViewer.getMainFrame());
                if ( ret == JFontChooser.OK_OPTION )
                {
                    HighlightManager.getInstance().getDefaultEntry().setFont(fc.getSelectedFont());
                    logContainer.defaultFontChanged(fc.getSelectedFont());
                }
            }
        });
        return toolbar;
    }

    private JToolBar createToolbar()
    {
        JToolBar toolbar = new JToolBar();
        FlowLayout fl = new FlowLayout(FlowLayout.LEADING, 4, 4);
        fl.setAlignOnBaseline(true);
        toolbar.setLayout(fl);
        toolbar.setSize(new Dimension(200, 48));
        toolbar.setFloatable(false);
        toolbar.add(createStandardToolbar());
        toolbar.add(createMavenToolbar());
        return toolbar;
    }

    private JSeparator createSeparator()
    {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 35));
        return sep;
    }

    private LogfilesContainer createLogfilesContainer()
    {
        return new TabBasedLogfilesContainer(this);
    }

    public static class GlassPane
            extends JPanel
    {

        @Override
        public void paint(Graphics g)
        {
            g.setColor(new Color(180, 180, 180, 128));
            g.fillRect(0, 0, getSize().width, getSize().height);
        }
    }

}
