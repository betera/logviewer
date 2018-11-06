package com.betera.logviewer.ui;

import com.betera.logviewer.Icons;
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
    private JPanel spacerPanel;

    public CollapsiblePanel(JComponent component, String title, boolean initiallyExpanded)
    {
        super();

        spacerPanel = new JPanel();

        setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), title));

        collapseAction = new CollapseAction(component);
        collapseButton = new JButton(collapseAction);
        collapseButton.setBorderPainted(false);
        collapseButton.setContentAreaFilled(false);
        collapseButton.setText("");
        collapseButton.setPreferredSize(new Dimension(Icons.collapseIcon.getIconWidth(),
                                                      Icons.collapseIcon.getIconHeight()));

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
        spacerPanel.setPreferredSize(new Dimension(calculateTextWidth(title) - Icons.collapseIcon.getIconWidth(),
                                                   component.getPreferredSize().height));
        if ( initiallyExpanded )
        {
            add(component, BorderLayout.CENTER);
        }
        else
        {
            add(spacerPanel, BorderLayout.CENTER);
            collapseAction.putValue(Action.SMALL_ICON, Icons.expandIcon);
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

    private class CollapseAction
            extends AbstractAction
    {
        private JComponent component;

        CollapseAction(JComponent component)
        {
            super("Collapse", Icons.collapseIcon);
            this.component = component;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if ( this.getValue(Action.SMALL_ICON) == Icons.collapseIcon )
            {
                remove(component);
                add(spacerPanel, BorderLayout.CENTER);
                this.putValue(Action.SMALL_ICON, Icons.expandIcon);
            }
            else
            {
                remove(spacerPanel);
                add(component, BorderLayout.CENTER);
                this.putValue(Action.SMALL_ICON, Icons.collapseIcon);
            }
            revalidate();
        }
    }

}
