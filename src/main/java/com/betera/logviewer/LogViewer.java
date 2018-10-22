package com.betera.logviewer;

import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.file.LogfilesContainer;
import com.betera.logviewer.file.TabBasedLogfilesContainer;
import com.betera.logviewer.file.column.LogfileParser;
import com.betera.logviewer.file.highlight.HighlightManager;
import com.betera.logviewer.ui.action.EditAction;
import com.betera.logviewer.ui.action.OpenFileAction;
import com.betera.logviewer.ui.maven.MavenConfigManager;
import com.betera.logviewer.ui.maven.MavenManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.ImageIcon;
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

public class LogViewer
{

    public static final int BOOKMARK_MAX_TITLE_SIZE = 40;

    public static final String PROP_LOGFILES = "pref.openLogfiles";
    private static final Object PROP_FOLLOW_TAIL = "pref.followTail";
    private static final int MAX_RECENT_FILE_SIZE = 10;
    private static final String VERSION = "0.1";

    private static JFrame mainFrame;

    private LogfilesContainer logContainer;

    private JCheckBox followTailCheckbox;
    private JCheckBox focusActiveCheckbox;
    private JPanel content;
    private List<String> recentFiles;

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
        new LogViewer();
    }

    public static void handleException(Exception exc)
    {
        StringWriter sOut = new StringWriter();
        PrintWriter out = new PrintWriter(sOut);
        exc.printStackTrace(out); // NOSONAR
        JOptionPane.showMessageDialog(null, "ERROR: " + sOut.toString());
    }

    public static JFrame getMainFrame()
    {
        return mainFrame;
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

        HighlightManager.getInstance().readHighlightConfig();
        LogfileParser.readColumnFormatterConfig();

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        mainFrame = new JFrame("LogViewer v" + VERSION);
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
                    MavenConfigManager.saveMavenConfig();
                    Debug.printStatistics();
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

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new EditAction("Highlights", HighlightManager.getInstance()));
        menuBar.add(editMenu);

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
        boolean followTail = "true".equalsIgnoreCase(pref.get(PROP_FOLLOW_TAIL) + "");
        followTailCheckbox.setSelected(followTail);
        logContainer.updateFollowTailCheckbox(followTail, null);

        MavenManager.init();

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
        tb.setLayout(new FlowLayout(FlowLayout.LEADING, 8, 2));
        tb.setBorderPainted(true);
        tb.setBorder(BorderFactory.createEtchedBorder());
        tb.setFloatable(false);
        tb.add(MavenManager.getProjectComboBox());
        tb.add(createSeparator());
        tb.add(MavenManager.getDoCleanCheckBox());
        tb.add(new JLabel("Goal:"));
        tb.add(MavenManager.getGoalComboBox());
        tb.add(createSeparator());
        tb.add(MavenManager.getForceUpdateComboBox());
        tb.add(MavenManager.getSkipTestsCheckBox());
        tb.add(MavenManager.getUseProfileCheckBox());
        tb.add(MavenManager.getProfileTextField());
        tb.add(createSeparator());
        tb.add(new JLabel("Deploy to:"));
        tb.add(MavenManager.getDeploymentComboBox());
        tb.add(createSeparator());

        tb.add(MavenManager.getAction(logContainer));

        return tb;
    }

    private JToolBar createToolbar()
    {
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
        toolbar.setSize(new Dimension(200, 48));
        toolbar.setFloatable(false);
        toolbar.add(new OpenFileAction(this, logContainer));

        followTailCheckbox = new JCheckBox("Follow Tail");
        followTailCheckbox.addItemListener(e -> {
            boolean doFollowTail = e.getStateChange() == ItemEvent.SELECTED;
            logContainer.fireFollowTailChanged(doFollowTail, null);

        });

        focusActiveCheckbox = new JCheckBox("Focus active");

        toolbar.add(createSeparator());
        toolbar.add(followTailCheckbox);
        toolbar.add(focusActiveCheckbox);
        toolbar.add(createMavenToolbar());
        return toolbar;
    }

    private JSeparator createSeparator()
    {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(4, 16));
        return sep;
    }

    private LogfilesContainer createLogfilesContainer()
    {
        return new TabBasedLogfilesContainer(this);
    }

}
