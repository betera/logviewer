package com.betera.logviewer.ui.maven;

import com.betera.logviewer.ui.edit.AbstractConfigPanel;
import com.betera.logviewer.ui.edit.DocumentTextAdapter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTextField;

public class MavenEditPanel
        extends AbstractConfigPanel
        implements ListSelectionListener
{
    private JXList deplList;
    private JPanel deplDetailPanel;
    private JXList projList;
    private JPanel projDetailPanel;
    private DeploymentModel deplModel;
    private ProjectModel projModel;
    private JXTextField deplName;
    private JXTextField deplPath;
    private AbstractAction deplDeleteAction;
    private AbstractAction deplCopyAction;
    private AbstractAction projDeleteAction;
    private AbstractAction projCopyAction;
    private JXTextField projName;
    private JXTextField projRoot;
    private JXTextField projEar;

    public MavenEditPanel()
    {
        super("Maven");
        setPreferredSize(new Dimension(600, 820));

        initDetailView();
        deplModel = new DeploymentModel();
        for ( MavenDeployment entry : MavenConfigManager.getInstance().getDeployments() )
        {
            deplModel.addElement(entry);
        }
        projModel = new ProjectModel();
        for ( MavenProject entry2 : MavenConfigManager.getInstance().getProjects() )
        {
            projModel.addElement(entry2);
        }

        deplList = new JXList();
        deplList.setModel(deplModel);
        deplList.setBackground(Color.WHITE);
        deplList.setOpaque(true);
        deplList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        deplList.setCellRenderer(new ListCellRenderer<MavenDeployment>()
        {
            @Override
            public Component getListCellRendererComponent(JList<? extends MavenDeployment> list,
                                                          MavenDeployment value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus)
            {
                JPanel pnl = new JPanel();
                pnl.setOpaque(true);
                if ( isSelected )
                {
                    pnl.setBackground(new Color(230, 230, 255));
                }
                else
                {
                    pnl.setBackground(Color.WHITE);
                }
                pnl.setLayout(new FlowLayout(FlowLayout.LEADING));

                JLabel lbl = new JLabel(value.getDeploymentName());
                lbl.setPreferredSize(new Dimension(160, 20));
                pnl.add(lbl);

                JLabel lbl2 = new JLabel(value.getDeploymentPath());
                lbl2.setPreferredSize(new Dimension(320, 20));
                pnl.add(lbl2);

                return pnl;
            }
        });

        deplList.addListSelectionListener(this);

        projList = new JXList();
        projList.setModel(projModel);
        projList.setBackground(Color.WHITE);
        projList.setOpaque(true);
        projList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        projList.setCellRenderer(new ListCellRenderer<MavenProject>()
        {
            @Override
            public Component getListCellRendererComponent(JList<? extends MavenProject> list,
                                                          MavenProject value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus)
            {
                JPanel pnl = new JPanel();
                pnl.setOpaque(true);
                if ( isSelected )
                {
                    pnl.setBackground(new Color(230, 230, 255));
                }
                else
                {
                    pnl.setBackground(Color.WHITE);
                }
                pnl.setLayout(new FlowLayout(FlowLayout.LEADING));

                JLabel lbl = new JLabel(value.getProjectName());
                lbl.setPreferredSize(new Dimension(120, 20));
                pnl.add(lbl);

                JLabel lbl2 = new JLabel(value.getRootDir());
                lbl2.setPreferredSize(new Dimension(190, 20));
                pnl.add(lbl2);
                JLabel lbl3 = new JLabel(value.getEarPath());
                lbl3.setPreferredSize(new Dimension(190, 20));
                pnl.add(lbl3);

                return pnl;
            }
        });

        projList.addListSelectionListener(this);

        getContentPanel().setLayout(new BorderLayout());

        JPanel deplPanel = new JPanel();
        deplPanel.setLayout(new BorderLayout());
        deplPanel.setBorder(new TitledBorder("Deployments"));

        deplPanel.add(new JScrollPane(deplList), BorderLayout.CENTER);
        deplPanel.add(createDeploymentActionBar(), BorderLayout.EAST);
        deplPanel.add(deplDetailPanel, BorderLayout.SOUTH);

        getContentPanel().add(deplPanel, BorderLayout.NORTH);

        JPanel projPanel = new JPanel();
        projPanel.setLayout(new BorderLayout());
        projPanel.setBorder(new TitledBorder("Projects"));

        projPanel.add(new JScrollPane(projList), BorderLayout.CENTER);
        projPanel.add(createProjectActionBar(), BorderLayout.EAST);
        projPanel.add(projDetailPanel, BorderLayout.SOUTH);

        getContentPanel().add(projPanel, BorderLayout.SOUTH);

        updateEnablement();
    }

    private JPanel createDeploymentActionBar()
    {
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(SwingConstants.VERTICAL);
        toolbar.setFloatable(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        Action newAction = new AbstractAction("New", new ImageIcon("./images/create.png"))
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                MavenDeployment entry = new MavenDeployment("New deployment", "Path to wildfly");
                deplModel.addElement(entry);
                deplList.setSelectedValue(entry, true);

            }
        };

        deplDeleteAction = new AbstractAction("Delete", new ImageIcon("./images/trashbin.png"))
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for ( int i = deplList.getSelectedIndices().length - 1; i >= 0; i-- )
                {
                    deplModel.remove(deplList.getSelectedIndices()[i]);
                }

                updateDeploymentDetailView();
                updateEnablement();
            }
        };

        deplCopyAction = new AbstractAction("Copy", new ImageIcon("./images/copy.png"))
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                for ( Object entryObj : deplList.getSelectedValues() )
                {
                    MavenDeployment entry = (MavenDeployment) entryObj;
                    deplModel.addElement(new MavenDeployment(entry.getDeploymentName() + " Copy",
                                                             entry.getDeploymentPath()));
                }
                deplList.repaint();
            }
        };

        toolbar.add(newAction);
        toolbar.add(deplCopyAction);
        toolbar.add(deplDeleteAction);
        deplDeleteAction.setEnabled(false);
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());
        pnl.add(toolbar, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createProjectActionBar()
    {
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(SwingConstants.VERTICAL);
        toolbar.setFloatable(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        Action newAction = new AbstractAction("New", new ImageIcon("./images/create.png"))
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                projModel.addElement(new MavenProject("New project", "Root directory", "EAR file"));
            }
        };

        projDeleteAction = new AbstractAction("Delete", new ImageIcon("./images/trashbin.png"))
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for ( int i = projList.getSelectedIndices().length - 1; i >= 0; i-- )
                {
                    projModel.remove(projList.getSelectedIndices()[i]);
                }

                updateProjectDetailView();
                updateEnablement();
            }
        };

        projCopyAction = new AbstractAction("Copy", new ImageIcon("./images/copy.png"))
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                for ( Object entryObj : projList.getSelectedValues() )
                {
                    MavenProject entry = (MavenProject) entryObj;
                    projModel.addElement(new MavenProject(entry.getProjectName() + " Copy",
                                                          entry.getRootDir(),
                                                          entry.getEarPath()));
                }
                projList.repaint();
            }
        };

        toolbar.add(newAction);
        toolbar.add(projCopyAction);
        toolbar.add(projDeleteAction);
        projDeleteAction.setEnabled(false);
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());
        pnl.add(toolbar, BorderLayout.CENTER);
        return pnl;
    }

    public List<MavenDeployment> getMavenDeployments()
    {
        return deplModel.getEntries();
    }

    private void initDetailView()
    {
        deplDetailPanel = new JPanel()
        {
            @Override
            public void setEnabled(boolean enabled)
            {
                super.setEnabled(enabled);
                for ( Component comp : getComponents() )
                {
                    comp.setEnabled(enabled);
                }
            }
        };
        deplDetailPanel.setLayout(new GridBagLayout());
        deplDetailPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        deplName = new JXTextField();
        deplName.setOuterMargin(new Insets(4, 4, 4, 4));
        deplName.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( deplList.getSelectedValues().length != 1 )
                {
                    return;
                }

                MavenDeployment e = (MavenDeployment) deplList.getSelectedValue();
                e.setDeploymentName(text);
            }
        });
        deplPath = new JXTextField();
        deplPath.setOuterMargin(new Insets(4, 4, 4, 4));
        deplPath.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( deplList.getSelectedValues().length != 1 )
                {
                    return;
                }

                MavenDeployment e = (MavenDeployment) deplList.getSelectedValue();
                e.setDeploymentPath(text);
            }
        });

        GridBagConstraints c1 = new GridBagConstraints();
        c1.weightx = 0.1;
        c1.insets = new Insets(4, 4, 4, 4);
        c1.gridx = 0;
        c1.gridwidth = 1;
        c1.fill = GridBagConstraints.BOTH;
        c1.anchor = GridBagConstraints.NORTHWEST;

        GridBagConstraints c2 = new GridBagConstraints();
        c2.weightx = 0.9;
        c2.insets = new Insets(4, 4, 4, 4);
        c2.gridx = 1;
        c2.gridwidth = GridBagConstraints.REMAINDER;
        c2.fill = GridBagConstraints.BOTH;
        c2.anchor = GridBagConstraints.NORTHWEST;

        deplDetailPanel.add(createLabel("Name:"), c1);
        deplDetailPanel.add(deplName, c2);
        deplDetailPanel.add(createLabel("Wildfly path:"), c1);
        deplDetailPanel.add(deplPath, c2);

        projDetailPanel = new JPanel()
        {
            @Override
            public void setEnabled(boolean enabled)
            {
                super.setEnabled(enabled);
                for ( Component comp : getComponents() )
                {
                    comp.setEnabled(enabled);
                }
            }
        };
        projDetailPanel.setLayout(new GridBagLayout());
        projDetailPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        projName = new JXTextField();
        projName.setOuterMargin(new Insets(4, 4, 4, 4));
        projName.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( projList.getSelectedValues().length != 1 )
                {
                    return;
                }

                MavenProject e = (MavenProject) projList.getSelectedValue();
                e.setProjectName(text);
            }
        });
        projRoot = new JXTextField();
        projRoot.setOuterMargin(new Insets(4, 4, 4, 4));
        projRoot.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( projList.getSelectedValues().length != 1 )
                {
                    return;
                }

                MavenProject e = (MavenProject) projList.getSelectedValue();
                e.setRootDir(text);
            }
        });
        projEar = new JXTextField();
        projEar.setOuterMargin(new Insets(4, 4, 4, 4));
        projEar.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( projList.getSelectedValues().length != 1 )
                {
                    return;
                }

                MavenProject e = (MavenProject) projList.getSelectedValue();
                e.setEarPath(text);
            }
        });

        c1 = new GridBagConstraints();
        c1.weightx = 0.1;
        c1.insets = new Insets(4, 4, 4, 4);
        c1.gridx = 0;
        c1.gridwidth = 1;
        c1.fill = GridBagConstraints.BOTH;
        c1.anchor = GridBagConstraints.NORTHWEST;

        c2 = new GridBagConstraints();
        c2.weightx = 0.9;
        c2.insets = new Insets(4, 4, 4, 4);
        c2.gridx = 1;
        c2.gridwidth = GridBagConstraints.REMAINDER;
        c2.fill = GridBagConstraints.BOTH;
        c2.anchor = GridBagConstraints.NORTHWEST;

        projDetailPanel.add(createLabel("Name:"), c1);
        projDetailPanel.add(projName, c2);
        projDetailPanel.add(createLabel("Root folder:"), c1);
        projDetailPanel.add(projRoot, c2);
        projDetailPanel.add(createLabel("EAR file:"), c1);
        projDetailPanel.add(projEar, c2);
    }

    private JLabel createLabel(String text)
    {
        JXLabel lbl = new JXLabel(text);
        lbl.setHorizontalAlignment(SwingConstants.LEADING);
        return lbl;
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {

        if ( e.getValueIsAdjusting() )
        {
            return;
        }

        updateEnablement();
        updateProjectDetailView();
        updateDeploymentDetailView();
    }

    private void updateDeploymentDetailView()
    {
        if ( deplList.getSelectedValues().length != 1 )
        {
            deplName.setText("");
            deplPath.setText("");
        }
        else
        {
            MavenDeployment e = (MavenDeployment) deplList.getSelectedValue();
            deplName.setText(e.getDeploymentName());
            deplPath.setText(e.getDeploymentPath());
        }

    }

    private void updateProjectDetailView()
    {

        if ( projList.getSelectedValues().length != 1 )
        {
            projName.setText("");
            projRoot.setText("");
            projEar.setText("");
        }
        else
        {
            MavenProject e = (MavenProject) projList.getSelectedValue();
            projName.setText(e.getProjectName());
            projRoot.setText(e.getRootDir());
            projEar.setText(e.getEarPath());
        }
    }

    private void updateEnablement()
    {
        deplDetailPanel.setEnabled(deplList.getSelectedValues().length == 1);
        deplDeleteAction.setEnabled(deplList.getSelectedValues().length > 0);
        deplCopyAction.setEnabled(deplList.getSelectedValues().length > 0);
        projDetailPanel.setEnabled(projList.getSelectedValues().length == 1);
        projDeleteAction.setEnabled(projList.getSelectedValues().length > 0);
        projCopyAction.setEnabled(projList.getSelectedValues().length > 0);
    }

    public List<MavenProject> getMavenProjects()
    {
        return projModel.getEntries();
    }

    private class DeploymentModel
            extends DefaultListModel<MavenDeployment>
    {

        public List<MavenDeployment> getEntries()
        {
            List<MavenDeployment> list = new ArrayList<>();
            for ( int i = 0; i < getSize(); i++ )
            {
                list.add(get(i));
            }

            return list;
        }

    }

    private class ProjectModel
            extends DefaultListModel<MavenProject>
    {
        public List<MavenProject> getEntries()
        {
            List<MavenProject> list = new ArrayList<>();
            for ( int i = 0; i < getSize(); i++ )
            {
                list.add(get(i));
            }

            return list;
        }
    }
}
