package com.betera.logviewer.ui.maven;

import com.betera.logviewer.file.LogfilesContainer;
import com.betera.logviewer.ui.action.RunMavenAction;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class MavenManager
{

    private static final String DEPLOYMENT_SUBDIR = "/standalone/deployments";
    private static RunMavenAction action;
    private static JComboBox<MavenProject> projectComboBox;
    private static JComboBox<MavenDeployment> deploymentComboBox;
    private static JComboBox goalComboBox;
    private static JCheckBox useProfileCheckBox;
    private static JCheckBox doCleanCheckBox;
    private static JCheckBox skipTestsCheckBox;
    private static JTextField profileTextField;
    private static JCheckBox forceUpdateComboBox;

    public static JCheckBox getForceUpdateComboBox()
    {
        if ( forceUpdateComboBox == null )
        {
            forceUpdateComboBox = new JCheckBox("-U");
        }
        return forceUpdateComboBox;
    }

    public static JCheckBox getSkipTestsCheckBox()
    {
        if ( skipTestsCheckBox == null )
        {
            skipTestsCheckBox = new JCheckBox("Skip tests");
        }

        return skipTestsCheckBox;
    }

    public static JCheckBox getUseProfileCheckBox()
    {
        if ( useProfileCheckBox == null )
        {
            useProfileCheckBox = new JCheckBox("-P");
        }

        return useProfileCheckBox;
    }

    public static JCheckBox getDoCleanCheckBox()
    {
        if ( doCleanCheckBox == null )
        {
            doCleanCheckBox = new JCheckBox("Clean");
        }
        return doCleanCheckBox;
    }

    public static JTextField getProfileTextField()
    {
        if ( profileTextField == null )
        {
            profileTextField = new JTextField();
            profileTextField.setColumns(14);
        }

        return profileTextField;
    }

    public static JComboBox getGoalComboBox()
    {
        if ( goalComboBox == null )
        {
            goalComboBox = new JComboBox();
            goalComboBox.addItem("test");
            goalComboBox.addItem("compile");
            goalComboBox.addItem("build");
            goalComboBox.addItem("install");
        }

        return goalComboBox;
    }

    public static JComboBox getDeploymentComboBox()
    {
        if ( deploymentComboBox == null )
        {
            deploymentComboBox = new JComboBox();
        }
        return deploymentComboBox;
    }

    public static JComboBox getProjectComboBox()
    {
        if ( projectComboBox == null )
        {
            projectComboBox = new JComboBox();
        }

        return projectComboBox;
    }

    public static RunMavenAction getAction(LogfilesContainer container)
    {
        if ( action == null )
        {
            action = new RunMavenAction(container);
        }

        return action;
    }

    public static void setAction(RunMavenAction action)
    {
        MavenManager.action = action;
    }

    public static void init()
    {
        MavenConfigManager.readConfig();

        for ( MavenProject proj : MavenConfigManager.getProjects() )
        {
            getProjectComboBox().addItem(proj);
        }
        for ( MavenDeployment depl : MavenConfigManager.getDeployments() )
        {
            getDeploymentComboBox().addItem(depl);
        }

        getProfileTextField().setText(MavenConfigManager.getDefaultProfile());
        getProjectComboBox().setSelectedItem(MavenConfigManager.getProjectByName(MavenConfigManager.getDefaultProject()));
        getDeploymentComboBox().setSelectedItem(MavenConfigManager.getDeploymentByName(MavenConfigManager.getDefaultDeployment()));
        getDoCleanCheckBox().setSelected(MavenConfigManager.isDefaultClean());
        getUseProfileCheckBox().setSelected(MavenConfigManager.isDefaultUseProfile());
        getGoalComboBox().setSelectedItem(MavenConfigManager.getDefaultGoal());
        getSkipTestsCheckBox().setSelected(MavenConfigManager.isDefaultTests());
        getForceUpdateComboBox().setSelected(MavenConfigManager.isDefaultForceUpdate());
    }

    public static void run(String rootDir, String targetEar, String params, String deployDir)
    {
        List<String> envVars = new ArrayList<>();

        String targetEarDir = new File(targetEar).getParent();
        String copyString = "";
        if ( deployDir != null )
        {
            copyString = targetEar + " " + deployDir + DEPLOYMENT_SUBDIR;
            copyString = "/Y " + copyString.replace("/", "\\");
            envVars.add("MR_COPY_STRING=" + copyString);
        }

        envVars.add("MR_POM_DIR=" + rootDir);
        envVars.add("MR_STRING=" + params);
        envVars.add("MR_EAR_DIR=" + targetEarDir);
        envVars.add("MR_PIPE= > c:\\projekte\\emes\\maven.log");

        for ( Entry<String, String> env : System.getenv().entrySet() )
        {
            envVars.add(env.getKey() + "=" + env.getValue());
        }

        try
        {
            Runtime.getRuntime().exec("cmd /c start /min \"\" mavenRunner.bat",
                                      envVars.toArray(new String[envVars.size()]));
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
