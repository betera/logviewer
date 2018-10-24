package com.betera.logviewer.ui.edit;

import com.betera.logviewer.LogViewer;
import java.awt.BorderLayout;
import javax.swing.JDialog;

public class ConfigDialog
        extends JDialog
{

    public ConfigDialog(ConfigEditUIProvider provider, AbstractConfigPanel editPanel)
    {
        super();
        editPanel.addConfigDialogClosedListener(new ConfigDialogClosedListener()
        {
            @Override
            public void dialogCancelled()
            {
                LogViewer.getGlassPane().setVisible(false);
                ConfigDialog.this.dispose();
            }

            @Override
            public void dialogSaved()
            {
                provider.updateConfig();
                LogViewer.getGlassPane().setVisible(false);
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

}
