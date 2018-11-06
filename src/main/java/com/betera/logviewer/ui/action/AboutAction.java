package com.betera.logviewer.ui.action;

import com.betera.logviewer.Icons;
import com.betera.logviewer.LogViewer;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class AboutAction
        extends AbstractAction
{

    public AboutAction()
    {
        super("About");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JLabel lbl = new JLabel();
        ImageIcon ic = Icons.aboutIcon;
        lbl.setIcon(ic);
        lbl.setPreferredSize(new Dimension(ic.getImage().getWidth(null), ic.getImage().getHeight(null)));

        JDialog dlg = new JDialog();
        dlg.setUndecorated(true);
        dlg.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        dlg.getContentPane().add(lbl, c);
        JLabel lbl2 = new JLabel("<html><center><div style='font-size:24px'>LogViewer " + LogViewer.VERSION
                                         + "</div>(c) Michael Bessert, 2018</center></html>");
        lbl2.setHorizontalAlignment(SwingConstants.CENTER);
        lbl2.setBorder(new EmptyBorder(10, 10, 10, 10));
        dlg.getContentPane().add(lbl2, c);
        dlg.pack();
        ((JComponent) dlg.getContentPane()).setBorder(new EtchedBorder());
        dlg.setVisible(true);
        dlg.setLocationRelativeTo(LogViewer.getMainFrame());
        dlg.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                dlg.setVisible(false);
                dlg.dispose();
            }
        });
    }
}
