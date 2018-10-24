package com.betera.logviewer.ui.action;

import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.file.LogfilesContainer;
import com.betera.logviewer.ui.maven.MavenDeployment;
import com.betera.logviewer.ui.maven.MavenManager;
import com.betera.logviewer.ui.maven.MavenProject;
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
    private boolean useProfile;
    private boolean doClean;
    private String profile;
    private String deploymentDir;
    private LogfilesContainer container;
    private boolean forceUpdate;

    public RunMavenAction(LogfilesContainer container)
    {
        super("", new ImageIcon("./images/play.png"));
        this.container = container;
    }

    private String buildParamString()
    {
        return (doClean ? "clean " : "") + goal + " " + (forceUpdate ? "-U " : "") + (skipTests
                ? "-DskipTests=true"
                : "") + " " + (profile != null ? "-P " + profile : "");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        projectDir = ((MavenProject) MavenManager.getInstance().getProjectComboBox().getSelectedItem()).getRootDir();
        earPath = ((MavenProject) MavenManager.getInstance().getProjectComboBox().getSelectedItem()).getEarPath();
        deploymentDir = ((MavenDeployment) MavenManager.getInstance()
                .getDeploymentComboBox()
                .getSelectedItem()).getDeploymentPath();
        goal = (String) MavenManager.getInstance().getGoalComboBox().getSelectedItem();
        doClean = MavenManager.getInstance().getDoCleanCheckBox().isSelected();
        skipTests = MavenManager.getInstance().getSkipTestsCheckBox().isSelected();
        forceUpdate = MavenManager.getInstance().getForceUpdateComboBox().isSelected();
        useProfile = MavenManager.getInstance().getUseProfileCheckBox().isSelected();
        profile = MavenManager.getInstance().getProfileTextField().getText();

        MavenManager.getInstance().run(projectDir, earPath, buildParamString(), deploymentDir);
        for ( Logfile logfile : container.getOpenLogfiles() )
        {
            if ( "maven.log".equals(logfile.getDisplayName()) )
            {
                container.focusLogfile(logfile);
                break;
            }
        }

    }

    public boolean isForceUpdate()
    {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate)
    {
        this.forceUpdate = forceUpdate;
    }

    public boolean isDoClean()
    {
        return doClean;
    }

    public void setDoClean(boolean doClean)
    {
        this.doClean = doClean;
    }

    public String getDeploymentDir()
    {
        return deploymentDir;
    }

    public void setDeploymentDir(String deploymentDir)
    {
        this.deploymentDir = deploymentDir;
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
