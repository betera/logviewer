package com.betera.logviewer.ui.maven;

import com.betera.logviewer.LogViewer;
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

    private static final MavenManager instance = new MavenManager();
    private static final String DEPLOYMENT_SUBDIR = "/standalone/deployments";
    private RunMavenAction action;
    private JComboBox<MavenProject> projectComboBox;
    private JComboBox<MavenDeployment> deploymentComboBox;
    private JComboBox goalComboBox;
    private JCheckBox useProfileCheckBox;
    private JCheckBox doCleanCheckBox;
    private JCheckBox skipTestsCheckBox;
    private JTextField profileTextField;
    private JCheckBox forceUpdateComboBox;
    private MavenEditPanel editPanel;

    public static synchronized MavenManager getInstance()
    {
        return instance;
    }

    public JCheckBox getForceUpdateComboBox()
    {
        if ( forceUpdateComboBox == null )
        {
            forceUpdateComboBox = new JCheckBox("-U");
        }
        return forceUpdateComboBox;
    }

    public JCheckBox getSkipTestsCheckBox()
    {
        if ( skipTestsCheckBox == null )
        {
            skipTestsCheckBox = new JCheckBox("Skip tests");
        }

        return skipTestsCheckBox;
    }

    public JCheckBox getUseProfileCheckBox()
    {
        if ( useProfileCheckBox == null )
        {
            useProfileCheckBox = new JCheckBox("-P");
        }

        return useProfileCheckBox;
    }

    public JCheckBox getDoCleanCheckBox()
    {
        if ( doCleanCheckBox == null )
        {
            doCleanCheckBox = new JCheckBox("Clean");
        }
        return doCleanCheckBox;
    }

    public JTextField getProfileTextField()
    {
        if ( profileTextField == null )
        {
            profileTextField = new JTextField();
            profileTextField.setColumns(14);
        }

        return profileTextField;
    }

    public JComboBox getGoalComboBox()
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

    public JComboBox getDeploymentComboBox()
    {
        if ( deploymentComboBox == null )
        {
            deploymentComboBox = new JComboBox();
        }
        return deploymentComboBox;
    }

    public JComboBox getProjectComboBox()
    {
        if ( projectComboBox == null )
        {
            projectComboBox = new JComboBox();
        }

        return projectComboBox;
    }

    public RunMavenAction getAction(LogfilesContainer container)
    {
        if ( action == null )
        {
            action = new RunMavenAction(container);
        }

        return action;
    }

    public void setAction(RunMavenAction action)
    {
        this.action = action;
    }

    public void init()
    {
        MavenConfigManager.getInstance().readConfig();

        getProjectComboBox().removeAllItems();
        for ( MavenProject proj : MavenConfigManager.getInstance().getProjects() )
        {
            getProjectComboBox().addItem(proj);
        }
        getDeploymentComboBox().removeAllItems();
        for ( MavenDeployment depl : MavenConfigManager.getInstance().getDeployments() )
        {
            getDeploymentComboBox().addItem(depl);
        }

        getProfileTextField().setText(MavenConfigManager.getInstance().getDefaultProfile());
        getProjectComboBox().setSelectedItem(MavenConfigManager.getInstance()
                                                     .getProjectByName(MavenConfigManager.getInstance()
                                                                               .getDefaultProject()));
        getDeploymentComboBox().setSelectedItem(MavenConfigManager.getInstance()
                                                        .getDeploymentByName(MavenConfigManager.getInstance()
                                                                                     .getDefaultDeployment()));
        getDoCleanCheckBox().setSelected(MavenConfigManager.getInstance().isDefaultClean());
        getUseProfileCheckBox().setSelected(MavenConfigManager.getInstance().isDefaultUseProfile());
        getGoalComboBox().setSelectedItem(MavenConfigManager.getInstance().getDefaultGoal());
        getSkipTestsCheckBox().setSelected(MavenConfigManager.getInstance().isDefaultTests());
        getForceUpdateComboBox().setSelected(MavenConfigManager.getInstance().isDefaultForceUpdate());
    }

    public void run(String rootDir, String targetEar, String params, String deployDir)
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

        for ( Entry<String, String> env : System.getenv().entrySet() )
        {
            envVars.add(env.getKey() + "=" + env.getValue());
        }

        try
        {
            String mvnLf = LogViewer.mavenLogfile == null || LogViewer.mavenLogfile.isEmpty()
                    ? ""
                    : " ^> " + LogViewer.mavenLogfile;
            Runtime.getRuntime().exec("cmd /c start /min \"\" mavenRunner.bat" + mvnLf,
                                      envVars.toArray(new String[envVars.size()]));
        }
        catch ( IOException e )
        {
            LogViewer.handleException(e);
        }
    }

}
