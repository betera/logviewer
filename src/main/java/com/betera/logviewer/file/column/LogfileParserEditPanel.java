package com.betera.logviewer.file.column;

import com.betera.logviewer.ui.EnablementInheritingJPanel;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
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

public class LogfileParserEditPanel
        extends AbstractConfigPanel
        implements ListSelectionListener
{

    private JXList rowList;
    private JPanel rowDetailPanel;
    private LogfileRowModel rowModel;
    private JXTextField rowName;
    private JXTextField rowMatcher;
    private AbstractAction rowDeleteAction;
    private AbstractAction rowCopyAction;
    private JXTextField rowIgnore;

    private JPanel columnPanel;
    private JXList columnList;
    private JPanel columnDetailPanel;
    private JXTextField columnName;
    private JXTextField columnType;
    private JXTextField columnMaxSize;
    private JCheckBox columnInitiallyHidden;
    private JXTextField columnParameter;
    private LogfileColumnModel columnModel;
    private AbstractAction columnDeleteAction;
    private AbstractAction columnCopyAction;
    private AbstractAction columnMoveUpAction;
    private AbstractAction columnMoveDownAction;

    public LogfileParserEditPanel()
    {
        super("Columns");
        setPreferredSize(new Dimension(600, 820));

        initRowDetailView();
        initColumnDetailView();
        rowModel = new LogfileRowModel();
        for ( LogfileRowConfig entry : LogfileParser.getInstance().getRowConfigs() )
        {
            rowModel.addElement(entry);
        }
        columnModel = new LogfileColumnModel();

        rowList = new JXList();
        rowList.setModel(rowModel);
        rowList.setBackground(Color.WHITE);
        rowList.setOpaque(true);
        rowList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rowList.setCellRenderer(new ListCellRenderer<LogfileRowConfig>()
        {
            @Override
            public Component getListCellRendererComponent(JList<? extends LogfileRowConfig> list,
                                                          LogfileRowConfig value,
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

                JLabel lbl = new JLabel(value.getName());
                lbl.setPreferredSize(new Dimension(160, 20));
                pnl.add(lbl);

                JLabel lbl2 = new JLabel(value.getMatcher());
                lbl2.setPreferredSize(new Dimension(320, 20));
                pnl.add(lbl2);

                return pnl;
            }
        });

        rowList.addListSelectionListener(this);

        getContentPanel().setLayout(new BorderLayout());

        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BorderLayout());
        rowPanel.setBorder(new TitledBorder("Logfiles"));

        rowPanel.add(new JScrollPane(rowList), BorderLayout.CENTER);
        rowPanel.add(createRowConfigActionBar(), BorderLayout.EAST);
        rowPanel.add(rowDetailPanel, BorderLayout.SOUTH);

        getContentPanel().add(rowPanel, BorderLayout.NORTH);

        columnList = new JXList();
        columnList.setModel(columnModel);
        columnList.setBackground(Color.WHITE);
        columnList.setOpaque(true);
        columnList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        columnList.setCellRenderer(new ListCellRenderer<LogfileColumnConfig>()
        {
            @Override
            public Component getListCellRendererComponent(JList<? extends LogfileColumnConfig> list,
                                                          LogfileColumnConfig value,
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

                pnl.add(createColumnListLabel(value.getColumnName(), 120));
                pnl.add(createColumnListLabel(value.getMaxColumnSize() + "", 40));
                pnl.add(createColumnListLabel(value.isInitiallyHidden() ? "\u2713" : "", 40));
                pnl.add(createColumnListLabel(value.getEntryType(), 120));
                pnl.add(createColumnListLabel(value.getParams() != null ? String.join(",", value.getParams()) : "",
                                              190));

                return pnl;
            }
        });

        columnList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if ( e.getValueIsAdjusting() )
                {
                    return;
                }

                if ( columnList.getSelectedValues() == null || columnList.getSelectedValues().length != 1 )
                {
                    columnPanel.setEnabled(false);
                }
                updateColumnDetailView();
            }
        });

        columnPanel = new EnablementInheritingJPanel();

        columnPanel.setLayout(new BorderLayout());
        columnPanel.setBorder(new TitledBorder("Columns"));

        columnPanel.add(new JScrollPane(columnList), BorderLayout.CENTER);
        columnPanel.add(createColumnConfigActionBar(), BorderLayout.EAST);
        columnPanel.add(columnDetailPanel, BorderLayout.SOUTH);

        getContentPanel().add(columnPanel, BorderLayout.CENTER);

        updateEnablement();
    }

    protected JLabel createColumnListLabel(String aText, int aWidth)
    {
        JLabel lbl = new JLabel(aText);
        lbl.setPreferredSize(new Dimension(aWidth, 20));
        return lbl;
    }

    private void updateColumnDetailView()
    {
        if ( columnList.getSelectedValues().length != 1 )
        {
            columnDetailPanel.setEnabled(false);
            columnName.setText("");
            columnParameter.setText("");
            columnType.setText("");
            columnMaxSize.setText("");
        }
        else
        {
            columnDetailPanel.setEnabled(true);
            LogfileColumnConfig config = (LogfileColumnConfig) columnList.getSelectedValues()[0];

            columnName.setText(config.getColumnName());
            columnType.setText(config.getEntryType());
            columnMaxSize.setText(config.getMaxColumnSize() + "");
            columnInitiallyHidden.setSelected(config.isInitiallyHidden());
            if ( config.getParams() == null )
            {
                columnParameter.setText("");
            }
            else
            {
                columnParameter.setText(String.join(" ", config.getParams()));
            }
        }

    }

    private void initColumnDetailView()
    {
        columnDetailPanel = new EnablementInheritingJPanel();
        columnDetailPanel.setLayout(new GridBagLayout());
        columnDetailPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.weighty = 0;
        columnName = new JXTextField();
        columnName.setOuterMargin(new Insets(5, 5, 5, 5));
        columnName.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( columnList.getSelectedValues().length != 1 )
                {
                    return;
                }

                LogfileColumnConfig e = (LogfileColumnConfig) columnList.getSelectedValue();
                e.setColumnName(text);
                columnList.repaint();
            }
        });
        columnDetailPanel.add(createLabel("Name:"), c);
        c.gridx = 1;
        c.weightx = 1;
        columnDetailPanel.add(columnName, c);
        c.gridx = 2;
        c.weightx = 0;
        columnDetailPanel.add(createLabel("Type:"), c);
        columnType = new JXTextField();
        columnType.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( columnList.getSelectedValues().length != 1 )
                {
                    return;
                }

                LogfileColumnConfig e = (LogfileColumnConfig) columnList.getSelectedValue();
                e.setEntryType(text);
                columnList.repaint();
            }
        });
        columnType.setOuterMargin(new Insets(5, 5, 5, 5));
        c.gridx = 3;
        c.weightx = 1;
        columnDetailPanel.add(columnType, c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        columnInitiallyHidden = new JCheckBox("Initially hidden");
        columnInitiallyHidden.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                if ( columnList.getSelectedValues().length != 1 )
                {
                    return;
                }

                LogfileColumnConfig e = (LogfileColumnConfig) columnList.getSelectedValue();
                e.setInitiallyHidden(columnInitiallyHidden.isSelected());
                columnList.repaint();
            }
        });
        columnDetailPanel.add(columnInitiallyHidden, c);
        c.gridx = 2;
        c.gridwidth = 1;
        columnDetailPanel.add(createLabel("Max size:"), c);
        columnMaxSize = new JXTextField();
        columnMaxSize.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( columnList.getSelectedValues().length != 1 )
                {
                    return;
                }

                LogfileColumnConfig e = (LogfileColumnConfig) columnList.getSelectedValue();
                e.setMaxColumnSize(Integer.valueOf(text));
                columnList.repaint();
            }
        });
        columnMaxSize.setOuterMargin(new Insets(5, 5, 5, 5));
        c.gridx = 3;
        columnDetailPanel.add(columnMaxSize, c);
        c.gridx = 0;
        c.gridy = 2;
        columnDetailPanel.add(createLabel("Parameter"), c);
        columnParameter = new JXTextField();
        columnParameter.setOuterMargin(new Insets(5, 5, 5, 5));
        c.gridwidth = 3;
        c.gridx = 1;
        columnDetailPanel.add(columnParameter, c);
        columnParameter.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( columnList.getSelectedValues().length != 1 )
                {
                    return;
                }

                LogfileColumnConfig e = (LogfileColumnConfig) columnList.getSelectedValue();
                e.setParams(text.split(" "));
                columnList.repaint();
            }
        });
    }

    private Component createColumnConfigActionBar()
    {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        return tb;
    }

    private JPanel createRowConfigActionBar()
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
                LogfileRowConfig entry = new LogfileRowConfig("New name", "New logfile matcher", new ArrayList<>());
                rowModel.addElement(entry);
                rowList.setSelectedValue(entry, true);

            }
        };

        rowDeleteAction = new AbstractAction("Delete", new ImageIcon("./images/trashbin.png"))
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for ( int i = rowList.getSelectedIndices().length - 1; i >= 0; i-- )
                {
                    rowModel.remove(rowList.getSelectedIndices()[i]);
                }

                updateRowConfigDetailView();
                updateEnablement();
            }
        };

        rowCopyAction = new AbstractAction("Copy", new ImageIcon("./images/copy.png"))
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                for ( Object entryObj : rowList.getSelectedValues() )
                {
                    LogfileRowConfig entry = (LogfileRowConfig) entryObj;
                    rowModel.addElement(new LogfileRowConfig(entry.getName() + " Copy",
                                                             entry.getMatcher(),
                                                             entry.getIgnoreList(),
                                                             entry.getEntries()));
                }
                rowList.repaint();
            }
        };

        toolbar.add(newAction);
        toolbar.add(rowCopyAction);
        toolbar.add(rowDeleteAction);
        rowDeleteAction.setEnabled(false);
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());
        pnl.add(toolbar, BorderLayout.CENTER);
        return pnl;
    }

    private void initRowDetailView()
    {
        rowDetailPanel = new EnablementInheritingJPanel();
        rowDetailPanel.setLayout(new GridBagLayout());
        rowDetailPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        rowName = new JXTextField();
        rowName.setOuterMargin(new Insets(4, 4, 4, 4));
        rowName.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( rowList.getSelectedValues().length != 1 )
                {
                    return;
                }

                LogfileRowConfig e = (LogfileRowConfig) rowList.getSelectedValue();
                e.setName(text);
                rowList.repaint();

            }
        });
        rowMatcher = new JXTextField();
        rowMatcher.setOuterMargin(new Insets(4, 4, 4, 4));
        rowMatcher.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( rowList.getSelectedValues().length != 1 )
                {
                    return;
                }

                LogfileRowConfig e = (LogfileRowConfig) rowList.getSelectedValue();
                e.setMatcher(text);
                rowList.repaint();

            }
        });
        rowIgnore = new JXTextField();
        rowIgnore.setOuterMargin(new Insets(4, 4, 4, 4));
        rowIgnore.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( rowList.getSelectedValues().length != 1 )
                {
                    return;
                }

                LogfileRowConfig e = (LogfileRowConfig) rowList.getSelectedValue();
                e.setToIgnore(Arrays.asList(text.split(" ")));
                rowList.repaint();
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

        rowDetailPanel.add(createLabel("Name:"), c1);
        rowDetailPanel.add(rowName, c2);
        rowDetailPanel.add(createLabel("Logfile Matcher:"), c1);
        rowDetailPanel.add(rowMatcher, c2);
        rowDetailPanel.add(createLabel("Ignore list:"), c1);
        rowDetailPanel.add(rowIgnore, c2);

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
        updateRowConfigDetailView();
        updateColumnList();
    }

    private void updateColumnList()
    {
        if ( rowList.getSelectedValues().length != 1 )
        {
            columnPanel.setEnabled(false);
        }

        columnModel.clear();
        LogfileRowConfig cfg = (LogfileRowConfig) rowList.getSelectedValues()[0];

        for ( LogfileColumnConfig colCfg : cfg.getEntries() )
        {
            columnModel.addElement(colCfg);
        }
    }

    private void updateRowConfigDetailView()
    {
        if ( rowList.getSelectedValues().length != 1 )
        {
            rowName.setText("");
            rowMatcher.setText("");
            rowIgnore.setText("");
        }
        else
        {
            LogfileRowConfig e = (LogfileRowConfig) rowList.getSelectedValue();
            rowName.setText(e.getName());
            rowMatcher.setText(e.getMatcher());
            rowIgnore.setText(e.getIgnoreList().stream().collect(Collectors.joining(" ")));
        }

    }

    private void updateEnablement()
    {
        rowDetailPanel.setEnabled(rowList.getSelectedValues().length == 1);
        rowDeleteAction.setEnabled(rowList.getSelectedValues().length > 0);
        rowCopyAction.setEnabled(rowList.getSelectedValues().length > 0);
    }

    private class LogfileRowModel
            extends DefaultListModel<LogfileRowConfig>
    {

    }

    private class LogfileColumnModel
            extends DefaultListModel<LogfileColumnConfig>
    {

    }
}
