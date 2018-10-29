package com.betera.logviewer.file.highlight;

import com.betera.logviewer.ui.edit.AbstractConfigPanel;
import com.betera.logviewer.ui.edit.DocumentTextAdapter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXColorSelectionButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTextField;

public class HighlightEditPanel
        extends AbstractConfigPanel
        implements ListSelectionListener, ListCellRenderer<HighlightEntry>
{

    private HighlightListModel model;
    private List<HighlightEntry> selectedEntries;
    private JXList list;
    private JPanel detailPanel;

    private JXTextField text;
    private JXColorSelectionButton fgColor;
    private JXColorSelectionButton bgColor;
    private JXTextField fontFamily;
    private JComboBox fontSize;
    private JCheckBox boldCheckbox;
    private JCheckBox bookmarkCheckbox;

    private Action deleteAction;
    private Action copyAction;

    public HighlightEditPanel()
    {
        super("Highlights");
        initDetailView();
        model = new HighlightListModel();
        for ( HighlightEntry entry : HighlightManager.getInstance().getEntries() )
        {
            model.addElement(entry.copy());
        }

        list = new JXList();
        list.setModel(model);
        list.setBackground(Color.WHITE);
        list.setOpaque(true);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setCellRenderer(this);
        list.addListSelectionListener(this);

        getContentPanel().setLayout(new BorderLayout());
        getContentPanel().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPanel().add(createActionBar(), BorderLayout.EAST);
        getContentPanel().add(detailPanel, BorderLayout.SOUTH);

        updateEnablement();
    }

    private JPanel createActionBar()
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
                HighlightEntry defaultEntry = HighlightManager.getInstance().getDefaultEntry();

                HighlightEntry entry = new HighlightEntry("New entry",
                                                          defaultEntry.getFont(),
                                                          defaultEntry.getForegroundColor(),
                                                          defaultEntry.getBackgroundColor(),
                                                          false);
                model.addElement(entry);
                list.setSelectedValue(entry, true);
            }
        };

        deleteAction = new AbstractAction("Delete", new ImageIcon("./images/trashbin.png"))
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for ( int i = list.getSelectedIndices().length - 1; i >= 0; i-- )
                {
                    model.remove(list.getSelectedIndices()[i]);
                }

                updateDetailView();
                updateEnablement();
            }
        };

        copyAction = new AbstractAction("Copy", new ImageIcon("./images/copy.png"))
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                for ( Object entryObj : list.getSelectedValues() )
                {
                    HighlightEntry entry = (HighlightEntry) entryObj;
                    model.addElement(new HighlightEntry(entry.getText() + " Copy",
                                                        entry.getFont(),
                                                        entry.getForegroundColor(),
                                                        entry.getBackgroundColor(),
                                                        entry.isAddBookmark()));
                }
                list.repaint();
            }
        };

        toolbar.add(newAction);
        toolbar.add(copyAction);
        toolbar.add(deleteAction);
        deleteAction.setEnabled(false);
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());
        pnl.add(toolbar, BorderLayout.CENTER);
        return pnl;
    }

    private void initDetailView()
    {
        detailPanel = new JPanel()
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
        detailPanel.setLayout(new GridLayout(3, 4));
        detailPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        text = new JXTextField();
        text.setOuterMargin(new Insets(4, 4, 4, 4));
        text.setColumns(16);
        text.addPropertyChangeListener(createPCListener("text", "Text"));
        text.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( list.getSelectedValues().length != 1 )
                {
                    return;
                }

                HighlightEntry e = (HighlightEntry) list.getSelectedValue();
                e.setText(text);
            }
        });
        fgColor = new JXColorSelectionButton();
        fgColor.setPreferredSize(new Dimension(16, 16));
        fgColor.addPropertyChangeListener(createPCListener("background", "ForegroundColor"));
        bgColor = new JXColorSelectionButton();
        bgColor.setPreferredSize(new Dimension(16, 16));
        bgColor.addPropertyChangeListener(createPCListener("background", "BackgroundColor"));
        fontFamily = new JXTextField();
        fontFamily.setColumns(12);
        fontFamily.setOuterMargin(new Insets(4, 4, 4, 4));
        fontFamily.addPropertyChangeListener(createPCListener("text", "FontFamily"));
        fontFamily.getDocument().addDocumentListener(new DocumentTextAdapter()
        {
            @Override
            public void textChanged(String text)
            {
                if ( list.getSelectedValues().length != 1 )
                {
                    return;
                }

                HighlightEntry e = (HighlightEntry) list.getSelectedValue();
                e.setFontFamily(text);
            }
        });
        boldCheckbox = new JCheckBox("Bold");
        boldCheckbox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                if ( list.getSelectedValues().length != 1 )
                {
                    return;
                }

                HighlightEntry e = (HighlightEntry) list.getSelectedValue();
                e.setBold(boldCheckbox.isSelected());
                list.repaint();
            }
        });

        bookmarkCheckbox = new JCheckBox("Bookmark");
        bookmarkCheckbox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                if ( list.getSelectedValues().length != 1 )
                {
                    return;
                }

                HighlightEntry e = (HighlightEntry) list.getSelectedValue();
                e.setAddBookmark(bookmarkCheckbox.isSelected());
                list.repaint();
            }
        });
        fontSize = new JComboBox();
        fontSize.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                if ( evt.getStateChange() == ItemEvent.SELECTED )
                {
                    if ( list.getSelectedValues().length != 1 )
                    {
                        return;
                    }

                    HighlightEntry e = (HighlightEntry) list.getSelectedValue();
                    e.setFontSize((Integer) evt.getItem());
                    list.repaint();

                }
            }
        });

        Integer[] fontSizes = new Integer[40];
        for ( int i = 0; i < fontSizes.length; i++ )
        {
            fontSizes[i] = i + 6;
        }
        fontSize.setModel(new DefaultComboBoxModel<Integer>(fontSizes));
        detailPanel.add(createLabel("Match:"));
        detailPanel.add(text);
        detailPanel.add(createLabel("Font:"));
        detailPanel.add(fontFamily);
        detailPanel.add(createLabel("Foreground:"));
        detailPanel.add(fgColor);
        detailPanel.add(createLabel("Size:"));
        detailPanel.add(fontSize);
        detailPanel.add(createLabel("Background:"));
        detailPanel.add(bgColor);
        detailPanel.add(boldCheckbox);
        detailPanel.add(bookmarkCheckbox);
    }

    private PropertyChangeListener createPCListener(final String property, final String method)
    {

        return new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if ( property.equals(evt.getPropertyName()) )
                {
                    if ( list.getSelectedValues().length != 1 )
                    {
                        return;
                    }

                    HighlightEntry e = (HighlightEntry) list.getSelectedValue();
                    try
                    {
                        Method method1 = e.getClass().getMethod("set" + method, evt.getNewValue().getClass());
                        Object newVal = evt.getNewValue();
                        if ( method1.getParameterTypes()[0] == Integer.class )
                        {
                            newVal = Integer.valueOf(newVal.toString());
                        }
                        method1.invoke(e, newVal);
                        list.repaint();
                    }
                    catch ( Exception e1 )
                    {
                        e1.printStackTrace();
                    }
                }
            }
        };
    }

    private JLabel createLabel(String text)
    {
        JXLabel lbl = new JXLabel(text);
        lbl.setHorizontalAlignment(SwingConstants.LEADING);
        return lbl;
    }

    public List<HighlightEntry> getHighlightEntries()
    {
        return ((HighlightListModel) model).getEntries();
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {

        if ( e.getValueIsAdjusting() )
        {
            return;
        }

        updateEnablement();
        updateDetailView();
    }

    private void updateDetailView()
    {
        if ( list.getSelectedValues().length != 1 )
        {
            text.setText("");
            fgColor.setBackground(Color.BLACK);
            bgColor.setBackground(Color.WHITE);
            fontFamily.setText("");
            fontSize.setSelectedIndex(0);
            boldCheckbox.setSelected(false);
            bookmarkCheckbox.setSelected(false);
        }
        else
        {
            HighlightEntry e = (HighlightEntry) list.getSelectedValue();
            text.setText(e.getText());
            fgColor.setBackground(e.getForegroundColor());
            bgColor.setBackground(e.getBackgroundColor());
            fontFamily.setText(e.getFont().getFamily());
            fontSize.setSelectedItem(e.getFont().getSize());
            boldCheckbox.setSelected(e.getFont().getStyle() != Font.PLAIN);
            bookmarkCheckbox.setSelected(e.isAddBookmark());
        }
    }

    private void updateEnablement()
    {
        detailPanel.setEnabled(list.getSelectedValues().length == 1);
        deleteAction.setEnabled(list.getSelectedValues().length > 0);
        copyAction.setEnabled(list.getSelectedValues().length > 0);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends HighlightEntry> list,
                                                  HighlightEntry value,
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

        JLabel lbl = new JLabel(value.getText());
        lbl.setPreferredSize(new Dimension(160, 20));
        pnl.add(lbl);

        JLabel fg = new JLabel();
        fg.setPreferredSize(new Dimension(16, 16));
        fg.setOpaque(true);
        fg.setBackground(value.getForegroundColor());
        pnl.add(fg);

        JLabel bg = new JLabel();
        bg.setPreferredSize(new Dimension(16, 16));
        bg.setOpaque(true);
        bg.setBackground(value.getBackgroundColor());
        pnl.add(bg);

        JLabel fnt = new JLabel();
        fnt.setText(value.getFont().getFamily());
        fnt.setPreferredSize(new Dimension(160, 20));
        pnl.add(fnt);

        JLabel fntSize = new JLabel();
        fntSize.setText(value.getFont().getSize() + "");
        fntSize.setPreferredSize(new Dimension(40, 20));
        pnl.add(fntSize);

        JLabel fntType = new JLabel();
        if ( value.getFont().getStyle() == Font.BOLD )
        {
            fntType.setText("\u2713");
        }
        fntType.setPreferredSize(new Dimension(20, 20));
        pnl.add(fntType);

        JLabel bmk = new JLabel();
        if ( value.isAddBookmark() )
        {
            bmk.setText("\u2713");
        }
        bmk.setPreferredSize(new Dimension(20, 20));
        pnl.add(bmk);

        return pnl;
    }

    protected class HighlightListModel
            extends DefaultListModel<HighlightEntry>
    {
        public List<HighlightEntry> getEntries()
        {
            List<HighlightEntry> list = new ArrayList<>();
            for ( int i = 0; i < getSize(); i++ )
            {
                list.add(get(i));
            }

            return list;
        }
    }
}
