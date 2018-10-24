package com.betera.logviewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

public class CollapsiblePanel
        extends JPanel
{

    private JButton collapseButton;
    private Action collapseAction;
    private ImageIcon collapseIcon;
    private ImageIcon expandIcon;
    private JPanel spacerPanel;

    public CollapsiblePanel(JComponent component, String title, boolean initiallyExpanded)
    {
        super();

        spacerPanel = new JPanel();

        setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), title));

        collapseIcon = new ImageIcon("./images/collapse.png");
        expandIcon = new ImageIcon("./images/expand.png");

        collapseAction = new AbstractAction("Collapse", collapseIcon)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ( this.getValue(Action.SMALL_ICON) == collapseIcon )
                {
                    remove(component);
                    add(spacerPanel, BorderLayout.CENTER);
                    this.putValue(Action.SMALL_ICON, expandIcon);
                }
                else
                {
                    remove(spacerPanel);
                    add(component, BorderLayout.CENTER);
                    this.putValue(Action.SMALL_ICON, collapseIcon);
                }
                revalidate();
            }
        };
        collapseButton = new JButton(collapseAction);
        collapseButton.setBorderPainted(false);
        collapseButton.setContentAreaFilled(false);
        collapseButton.setText("");
        collapseButton.setPreferredSize(new Dimension(collapseIcon.getIconWidth(), collapseIcon.getIconHeight()));

        setLayout(new BorderLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridx = 0;
        c.gridy = 0;

        JPanel tmp = new JPanel();
        tmp.setLayout(new BorderLayout());
        tmp.add(collapseButton, BorderLayout.WEST);
        tmp.add(new JPanel(), BorderLayout.CENTER);

        add(tmp, BorderLayout.WEST);
        c.anchor = GridBagConstraints.NORTH;
        c.gridheight = 1000;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 1;
        c.gridy = 0;
        spacerPanel.setPreferredSize(new Dimension(calculateTextWidth(title) - collapseIcon.getIconWidth(),
                                                   component.getPreferredSize().height));
        if ( initiallyExpanded )
        {
            add(component, BorderLayout.CENTER);
        }
        else
        {
            add(spacerPanel, BorderLayout.CENTER);
            collapseAction.putValue(Action.SMALL_ICON, expandIcon);
        }
    }

    private int calculateTextWidth(String title)
    {
        Font font = UIManager.getFont("Label.font");
        return (int) (this.getFontMetrics(font).stringWidth(title) * 1.2);
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior()
    {
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    public int getBaseline(int width, int height)
    {
        return 0;
    }

}
