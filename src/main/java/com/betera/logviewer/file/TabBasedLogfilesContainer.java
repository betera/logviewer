package com.betera.logviewer.file;

import com.betera.logviewer.LogViewer;
import com.betera.logviewer.ui.fileviewer.JTextPaneLogfile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

    @Override
    public void addFile(File file)
    {
        Logfile logfile = new JTextPaneLogfile(this, file, new LogfileConfiguration());
        logfiles.add(logfile);
        component.addTab(logfile.getName(), logfile.getComponent());
        component.setTabComponentAt(component.getTabCount() - 1, new LogfileTab(logfile));
        logfileMap.put(logfile.getName(), logfile);
        logfile.addLogfileStateChangedListener(this);

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
        if ( index != 0 )
        {
            component.setSelectedIndex(index);
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
    }

    public void contentChanged(boolean hasChanges, Logfile logfile)
    {
        LogfileTab tab = getTabForLogfile(logfile);
        if ( tab != null && !component.getSelectedComponent().equals(tab) )
        {
            tab.setHasChanges(hasChanges && !isLogfileFocused(logfile));
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

        private JLabel title;

        private ChangesPanel hasChangesPanel;

        private JButton closeButton;

        private boolean showFullName = false;

        public LogfileTab(Logfile aLogfile)
        {
            this.logfile = aLogfile;

            setLayout(new BorderLayout());

            setOpaque(false);

            hasChangesPanel = new ChangesPanel();
            title = new JLabel(aLogfile.getDisplayName());
            title.setOpaque(false);
            closeButton = new JButton();
            closeButton.setSize(new Dimension(16, 16));
            closeButton.setPreferredSize(new Dimension(16, 16));
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setIcon(new ImageIcon("./images/close.png"));
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
                g.setColor(Color.GRAY);
                g.fillOval(0, 0, getWidth() / 2, getHeight() / 2);
            }
        }
    }

}
