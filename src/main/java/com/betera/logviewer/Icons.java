package com.betera.logviewer;

import javax.swing.ImageIcon;

public class Icons
{

    public static final ImageIcon collapseIcon;
    public static final ImageIcon expandIcon;
    public static final ImageIcon createIcon;
    public static final ImageIcon deleteIcon;
    public static final ImageIcon copyIcon;
    public static final ImageIcon arrowUpIcon;
    public static final ImageIcon arrowDownIcon;
    public static final ImageIcon closeIcon;
    public static final ImageIcon packIcon;
    public static final ImageIcon wrapOnIcon;
    public static final ImageIcon wrapOffIcon;
    public static final ImageIcon tableIcon;
    public static final ImageIcon textPaneIcon;
    public static final ImageIcon clearFileIcon;
    public static final ImageIcon openIcon;
    public static final ImageIcon playIcon;
    public static final ImageIcon browseIcon;
    public static final ImageIcon tailIcon;
    public static final ImageIcon logViewerIcon;
    public static final ImageIcon tailCheckedIcon;
    public static final ImageIcon focusIcon;
    public static final ImageIcon focusCheckedIcon;
    public static final ImageIcon cancelIcon;
    public static final ImageIcon okIcon;
    public static final ImageIcon aboutIcon;
    public static final ImageIcon refreshIcon;

    static
    {
        collapseIcon = load("collapse.png");
        expandIcon = load("expand.png");
        createIcon = load("create.png");
        deleteIcon = load("trashbin.png");
        copyIcon = load("copy.png");
        arrowUpIcon = load("arrowUp.png");
        arrowDownIcon = load("arrowDown.png");
        closeIcon = load("close.png");
        packIcon = load("pack.png");
        wrapOnIcon = load("wrapOn.png");
        wrapOffIcon = load("wrapOff.png");
        tableIcon = load("table.png");
        textPaneIcon = load("textPane.png");
        clearFileIcon = load("clearFile.png");
        openIcon = load("open.png");
        playIcon = load("play.png");
        browseIcon = load("browse.png");
        logViewerIcon = load("logviewer.png");
        tailIcon = load("tail.png");
        tailCheckedIcon = load("tail_checked.png");
        focusIcon = load("focus.png");
        focusCheckedIcon = load("focus_checked.png");
        cancelIcon = load("cancel.png");
        okIcon = load("ok.png");
        aboutIcon = load("codemaster.jpg");
        refreshIcon = load("refresh.png");
    }

    private Icons()
    {
    }

    static ImageIcon load(String iconPath)
    {
        try
        {
            return new ImageIcon(Icons.class.getResource("/images/" + iconPath));
        }
        catch ( NullPointerException npe )
        {
            LogViewer.handleException(npe, "Icon \"/images/" + iconPath + "\" not found.");
        }
        return null;
    }

}
