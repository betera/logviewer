package com.betera.logviewer.ui.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class RunMavenAction
        extends AbstractAction
{

    private String projectDir;

    private String earPath;

    private String goal;

    private boolean skipTests;

    private String profile;

    public RunMavenAction()
    {
        super("", new ImageIcon("./images/play.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {

    }

    public String getProjectDir()
    {
        return projectDir;
    }

    public void setProjectDir(String projectDir)
    {
        this.projectDir = projectDir;
    }

    public String getEarPath()
    {
        return earPath;
    }

    public void setEarPath(String earPath)
    {
        this.earPath = earPath;
    }

    public String getGoal()
    {
        return goal;
    }

    public void setGoal(String aGoal)
    {
        this.goal = aGoal;
    }

    public boolean isSkipTests()
    {
        return skipTests;
    }

    public void setSkipTests(boolean skipTests)
    {
        this.skipTests = skipTests;
    }

    public String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        this.profile = profile;
    }

}
