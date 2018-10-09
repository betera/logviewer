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
        projectDir = ((MavenProject) MavenManager.getProjectComboBox().getSelectedItem()).getRootDir();
        earPath = ((MavenProject) MavenManager.getProjectComboBox().getSelectedItem()).getEarPath();
        deploymentDir = ((MavenDeployment) MavenManager.getDeploymentComboBox().getSelectedItem()).getDeploymentPath();
        goal = (String) MavenManager.getGoalComboBox().getSelectedItem();
        doClean = MavenManager.getDoCleanCheckBox().isSelected();
        skipTests = MavenManager.getSkipTestsCheckBox().isSelected();
        forceUpdate = MavenManager.getForceUpdateComboBox().isSelected();
        useProfile = MavenManager.getUseProfileCheckBox().isSelected();
        profile = MavenManager.getProfileTextField().getText();

        MavenManager.run(projectDir, earPath, buildParamString(), deploymentDir);
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
