package com.betera.logviewer.ui.edit;

import com.betera.logviewer.LogViewer;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;

public class ConfigDialog
        extends JDialog
{

    public ConfigDialog(ConfigEditUIProvider provider, AbstractConfigPanel editPanel)
    {
        super();
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                ConfigDialog.this.dispose();
            }
        });
        editPanel.addConfigDialogClosedListener(new ConfigDialogClosedListener()
        {
            @Override
            public void dialogCancelled()
            {
                ConfigDialog.this.dispose();
            }

            @Override
            public void dialogSaved()
            {
                provider.updateConfig();
                ConfigDialog.this.dispose();
            }
        });
        LogViewer.getGlassPane().setVisible(true);
        setUndecorated(true);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(editPanel, BorderLayout.CENTER);
        setModal(true);
        setTitle(editPanel.getTitle());
        pack();
        setLocationRelativeTo(LogViewer.getMainFrame());

    }

    @Override
    public void dispose()
    {
        LogViewer.getGlassPane().setVisible(false);
        super.dispose();
    }

    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        if ( !isVisible() )
        {
            dispose();
        }
    }
}
