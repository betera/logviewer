package com.betera.logviewer.ui;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class LogViewerLookAndFeel
        extends WindowsLookAndFeel
{

    private int logViewerFontSize = 14;
    private String logViewerFontName = "Segoe UI, Dialog";

    public void setFontSize(int size)
    {
        logViewerFontSize = size;
    }

    @Override
    protected void initComponentDefaults(UIDefaults table)
    {
        super.initComponentDefaults(table);

        initFont();
    }

    private void initFont()
    {
        UIManager.put("MenuItem.arrowIcon", new EmptyIcon());
        UIManager.put("MenuItem.checkIcon", new EmptyIcon());

        UIManager.put("InternalFrame.normalTitleFont", getFont(0));
        UIManager.put("TableHeader.font", getFont(0));
        UIManager.put("ToolTip.font", getFont(0));
        UIManager.put("Tree.font", getFont(0));
        UIManager.put("Label.font", getFont(0));
        UIManager.put(".font", getFont(0));

        UIManager.put("Button.font", getFont(0));
        UIManager.put("ToggleButton.font", getFont(0));
        UIManager.put("RadioButton.font", getFont(0));
        UIManager.put("CheckBox.font", getFont(0));
        UIManager.put("ColorChooser.font", getFont(0));
        UIManager.put("ComboBox.font", getFont(0));
        UIManager.put("Label.font", getFont(0));
        UIManager.put("List.font", getFont(0));
        UIManager.put("MenuBar.font", getFont(0));
        UIManager.put("MenuItem.font", getFont(0));
        UIManager.put("RadioButtonMenuItem.font", getFont(0));
        UIManager.put("CheckBoxMenuItem.font", getFont(0));
        UIManager.put("Menu.font", getFont(0));
        UIManager.put("PopupMenu.font", getFont(0));
        UIManager.put("OptionPane.font", getFont(0));
        UIManager.put("Panel.font", getFont(0));
        UIManager.put("ProgressBar.font", getFont(0));
        UIManager.put("ScrollPane.font", getFont(0));
        UIManager.put("Viewport.font", getFont(0));
        UIManager.put("TabbedPane.font", getFont(2));
        UIManager.put("Table.font", getFont(0));
        UIManager.put("TableHeader.font", getFont(0));
        UIManager.put("TextField.font", getFont(0));
        UIManager.put("PasswordField.font", getFont(0));
        UIManager.put("TextArea.font", getFont(0));
        UIManager.put("TextPane.font", getFont(0));
        UIManager.put("EditorPane.font", getFont(0));
        UIManager.put("TitledBorder.font", getFont(2));
        UIManager.put("ToolBar.font", getFont(0));
        UIManager.put("ToolTip.font", getFont(0));
        UIManager.put("Tree.font", getFont(0));
    }

    protected Font getFont(String fontName, int fontStyle, int fontSize)
    {
        return new Font(fontName, fontStyle, fontSize);
    }

    protected Font getFont(int size)
    {
        return new Font(logViewerFontName, 0, logViewerFontSize + size);
    }

    static public class EmptyIcon
            implements Icon
    {
        private int width = 0;
        private int height = 0;

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
        }

        public int getIconWidth()
        {
            return width;
        }

        public int getIconHeight()
        {
            return height;
        }
    }

}
