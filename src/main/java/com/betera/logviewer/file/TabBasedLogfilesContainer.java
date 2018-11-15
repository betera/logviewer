package com.betera.logviewer.file;

import com.betera.logviewer.Icons;
import com.betera.logviewer.LogViewer;
import com.betera.logviewer.ui.fileviewer.JTextPaneLogfile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;

public class TabBasedLogfilesContainer
        implements LogfilesContainer, LogfileStateChangedListener
{

    private JTabbedPane component;

    private List<Logfile> logfiles;

    private LogViewer logViewer;

    private Map<String, Logfile> logfileMap;

    public TabBasedLogfilesContainer(LogViewer logViewer)
    {
        this.logViewer = logViewer;
        component = new JTabbedPane();
        component.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                for ( int i = 0; i < component.getTabCount(); i++ )
                {
                    LogfileTab tab = (LogfileTab) component.getTabComponentAt(i);
                    if ( tab != null )
                    {
                        tab.setIsActive(i == component.getSelectedIndex());
                    }
                }
            }
        });
        component.setBackground(Color.GRAY);
        logfiles = new ArrayList<>();
        logfileMap = new HashMap<>();

        component.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                if ( component.getSelectedIndex() < 0 )
                {
                    return;
                }

                Component comp = component.getTabComponentAt(component.getSelectedIndex());
                if ( comp instanceof LogfileTab )
                {
                    LogfileTab selectedTab = (LogfileTab) comp;
                    if ( selectedTab != null )
                    {
                        selectedTab.setHasChanges(false);
                    }
                }
            }
        });
    }

    @Override
    public JComponent getComponent()
    {
        return component;
    }

    public void addRecentFile(String filePath)
    {

    }

    @Override
    public void addFile(File file)
    {
        Logfile logfile = new JTextPaneLogfile(this, file, new LogfileConfiguration());
        logfiles.add(logfile);
        component.addTab(logfile.getName(), logfile.getComponent());
        component.setTabComponentAt(component.getTabCount() - 1, new LogfileTab(logfile));
        logfileMap.put(logfile.getName(), logfile);
        logfile.addLogfileStateChangedListener(this);
        focusLogfile(logfile);
    }

    public boolean isLogfileFocused(Logfile logfile)
    {
        if ( logfile == null || component.getSelectedIndex() < 0 )
        {
            return false;
        }

        Component comp = component.getTabComponentAt(component.getSelectedIndex());
        if ( comp instanceof LogfileTab )
        {
            LogfileTab selectedTab = (LogfileTab) comp;
            if ( selectedTab.getLogfile().equals(logfile) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void focusLogfile(Logfile aLogfile)
    {
        int index = getIndexForLogfile(aLogfile);
        if ( index > -1 )
        {
            component.setSelectedIndex(index);
        }
    }

    @Override
    public void defaultFontChanged(Font newFont)
    {
        for ( Logfile logfile : logfiles )
        {
            logfile.defaultFontChanged(newFont);
        }
    }

    private LogfileTab getTabForLogfile(Logfile logfile)
    {
        for ( int i = 0; i < component.getTabCount(); i++ )
        {
            LogfileTab tab = (LogfileTab) component.getTabComponentAt(i);
            if ( tab.getLogfile().equals(logfile) )
            {
                return tab;
            }
        }
        return null;
    }

    private int getIndexForLogfile(Logfile logfile)
    {
        for ( int i = 0; i < component.getTabCount(); i++ )
        {
            LogfileTab tab = (LogfileTab) component.getTabComponentAt(i);
            if ( tab.getLogfile().equals(logfile) )
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void removeLogfile(Logfile file)
    {
        int indexToRemove = -1;

        for ( int i = 0; i < component.getTabCount(); i++ )
        {
            LogfileTab tab = (LogfileTab) component.getTabComponentAt(i);
            if ( tab.getLogfile().equals(file) )
            {
                indexToRemove = i;
                break;
            }
        }

        if ( indexToRemove >= 0 )
        {
            component.removeTabAt(indexToRemove);
        }
        logfiles.remove(file);
        logfileMap.put(file.getName(), null);
        file.destroy();
        logViewer.addRecentFile(file.getAbsolutePath());
    }

    public void contentChanged(boolean hasChanges, Logfile logfile)
    {
        LogfileTab tab = getTabForLogfile(logfile);
        if ( tab != null && !component.getSelectedComponent().equals(tab) )
        {
            tab.setHasChanges(hasChanges && !isLogfileFocused(logfile));
            if ( hasChanges && !isLogfileFocused(logfile) && logViewer.getFocusActiveCheckbox().isSelected() )
            {
                focusLogfile(logfile);
            }
        }
    }

    public void fireFollowTailChanged(boolean doFollowTail, Logfile ignored)
    {
        for ( Logfile logfile : logfiles )
        {
            if ( !logfile.equals(ignored) )
            {
                logfile.followTailChanged(doFollowTail);
            }
        }
    }

    @Override
    public void updateFollowTailCheckbox(boolean doFollowTail, Logfile source)
    {
        if ( source != null && !isLogfileFocused(source) )
        {
            return;
        }
        logViewer.getFollowTailCheckbox().setSelected(doFollowTail);
        fireFollowTailChanged(doFollowTail, source);
    }

    @Override
    public List<Logfile> getOpenLogfiles()
    {
        return logfiles;
    }

    private class LogfileTab
            extends JPanel
    {

        private Logfile logfile;

        private TitleLabel title;

        private ChangesPanel hasChangesPanel;

        private JButton closeButton;

        private boolean showFullName = false;

        public LogfileTab(Logfile aLogfile)
        {
            this.logfile = aLogfile;

            setLayout(new BorderLayout());

            setOpaque(false);

            hasChangesPanel = new ChangesPanel();
            title = new TitleLabel(aLogfile.getDisplayName());
            title.setFont(UIManager.getFont("TabbedPane.font"));
            title.setOpaque(false);
            closeButton = new JButton();
            closeButton.setUI(new BasicButtonUI());
            closeButton.setSize(new Dimension(20, 20));
            closeButton.setPreferredSize(new Dimension(20, 20));
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setIcon(Icons.closeIcon);
            closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    TabBasedLogfilesContainer.this.removeLogfile(logfile);
                }
            });

            add(title, BorderLayout.CENTER);

            JPanel pnl = new JPanel();
            pnl.setOpaque(false);

            pnl.add(hasChangesPanel);
            pnl.add(closeButton);

            add(pnl, BorderLayout.EAST);

        }

        public Logfile getLogfile()
        {
            return logfile;
        }

        public void setHasChanges(boolean aChangesFlag)
        {
            hasChangesPanel.setHasChanges(aChangesFlag);
            repaint();
        }

        public void setIsActive(boolean isActive)
        {
            title.setIsActive(isActive);
        }

        private class TitleLabel
                extends JLabel
        {
            private boolean isActive;

            public TitleLabel(String displayName)
            {
                super(displayName);
            }

            public void setIsActive(boolean anActiveFlag)
            {
                setFont(getFont().deriveFont(anActiveFlag ? Font.BOLD : Font.PLAIN));
                setForeground(anActiveFlag ? Color.white : Color.black);
            }

        }

    }

    private class ChangesPanel
            extends JPanel
    {

        boolean hasChanges = false;

        public void setHasChanges(boolean aChangesFlag)
        {
            hasChanges = aChangesFlag;
            repaint();
        }

        @Override
        public Dimension getPreferredSize()
        {
            return new Dimension(14, 14);
        }

        @Override
        public void paint(Graphics g)
        {
            if ( hasChanges )
            {
                g.setColor(new Color(60, 200, 60));
                g.fillOval(0, 0, (int) (getWidth() / 1.5), (int) (getHeight() / 1.5));
                g.setColor(Color.BLACK);
                g.drawOval(0, 0, (int) (getWidth() / 1.5), (int) (getHeight() / 1.5));
            }
            else
            {
//                g.setColor(Color.GRAY);
//                g.fillOval(0, 0, getWidth() / 2, getHeight() / 2);
            }
        }
    }

}
