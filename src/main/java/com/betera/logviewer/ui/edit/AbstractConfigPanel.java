package com.betera.logviewer.ui.edit;

import com.betera.logviewer.ui.MyWindowsButtonUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

public class AbstractConfigPanel
        extends JPanel
{

    protected List<ConfigDialogClosedListener> closedListener;
    protected String title;
    private JPanel contentPanel;
    public AbstractConfigPanel(String title)
    {
        this.title = title;
        closedListener = new ArrayList<>();
        setPreferredSize(new Dimension(600, 600));
        setLayout(new BorderLayout());
        setBorder(UIManager.getBorder("InternalFrame.border"));
        add(createNavigationPanel(), BorderLayout.SOUTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createTitlePanel(), BorderLayout.NORTH);
    }

    public String getTitle()
    {
        return title;
    }

    public JPanel getContentPanel()
    {
        return contentPanel;
    }

    protected JPanel createTitlePanel()
    {

        JPanel pnl = new JPanel();
        pnl.setOpaque(true);
        pnl.setBackground(new Color(245, 245, 245));
        pnl.setBorder(new LineBorder(Color.LIGHT_GRAY));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI, Arial", Font.PLAIN, 24));
        pnl.add(lbl);

        return pnl;
    }

    public void addConfigDialogClosedListener(ConfigDialogClosedListener listener)
    {
        if ( !closedListener.contains(listener) )
        {
            closedListener.add(listener);
        }
    }

    protected void fireDialogClosed(boolean isCancelled)
    {
        for ( ConfigDialogClosedListener listeners : closedListener )
        {
            if ( isCancelled )
            {
                listeners.dialogCancelled();
            }
            else
            {
                listeners.dialogSaved();
            }
        }
    }

    protected JPanel createContentPanel()
    {
        if ( contentPanel == null )
        {
            contentPanel = new JPanel();
            contentPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
        }
        return contentPanel;
    }

    protected JPanel createNavigationPanel()
    {
        JPanel pnl = new JPanel();
        pnl.setBorder(new LineBorder(Color.LIGHT_GRAY));
        pnl.setOpaque(true);
        pnl.setBackground(new Color(245, 245, 245));
        pnl.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JButton cancelBtn = new JButton();
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setIcon(new ImageIcon("./images/cancel.png"));
        cancelBtn.setFocusable(false);
        cancelBtn.setUI(new MyWindowsButtonUI());
        cancelBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fireDialogClosed(true);
            }
        });
        JButton okBtn = new JButton();
        okBtn.setFocusable(false);
        okBtn.setContentAreaFilled(false);
        okBtn.setBorderPainted(false);
        okBtn.setIcon(new ImageIcon("./images/ok.png"));
        okBtn.setUI(new MyWindowsButtonUI());

        okBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fireDialogClosed(false);
            }
        });

        pnl.add(okBtn);
        pnl.add(cancelBtn);
        return pnl;
    }

}
