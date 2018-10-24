package com.betera.logviewer.ui.maven;

import com.betera.logviewer.LogViewer;
import com.betera.logviewer.ui.edit.ConfigDialog;
import com.betera.logviewer.ui.edit.ConfigEditUIProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MavenConfigManager
        implements ConfigEditUIProvider
{
    private static final MavenConfigManager instance = new MavenConfigManager();

    private List<MavenProject> projects;

    private List<MavenDeployment> deployments;

    private String defaultGoal;

    private String defaultProject;

    private boolean defaultUseProfile;
    private boolean defaultTests;
    private String defaultProfile;
    private boolean defaultClean;
    private String defaultDeployment;
    private boolean defaultForceUpdate;
    private MavenEditPanel editPanel;

    public static synchronized MavenConfigManager getInstance()
    {
        return instance;
    }

    public boolean isDefaultForceUpdate()
    {
        return defaultForceUpdate;
    }

    public boolean isDefaultTests()
    {
        return defaultTests;
    }

    public String getDefaultDeployment()
    {
        return defaultDeployment;
    }

    public String getDefaultGoal()
    {
        return defaultGoal;
    }

    public String getDefaultProject()
    {
        return defaultProject;
    }

    public boolean isDefaultUseProfile()
    {
        return defaultUseProfile;
    }

    public String getDefaultProfile()
    {
        return defaultProfile;
    }

    public boolean isDefaultClean()
    {
        return defaultClean;
    }

    public List<MavenProject> getProjects()
    {
        if ( projects == null )
        {
            projects = new ArrayList<>();
        }

        return projects;
    }

    public List<MavenDeployment> getDeployments()
    {
        if ( deployments == null )
        {
            deployments = new ArrayList<>();
        }

        return deployments;
    }

    public MavenProject getProjectByName(String aName)
    {
        for ( MavenProject prj : getProjects() )
        {
            if ( prj.getProjectName().equals(aName) )
            {
                return prj;
            }
        }
        return null;
    }

    public MavenDeployment getDeploymentByName(String aName)
    {
        for ( MavenDeployment depl : getDeployments() )
        {
            if ( depl.getDeploymentName().equals(aName) )
            {
                return depl;
            }
        }
        return null;
    }

    public void saveMavenConfig()
    {
        File f = new File("maven.config");
        if ( f.exists() )
        {
            f.delete();
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(f)))
        {
            for ( MavenProject proj : getProjects() )
            {
                out.println("[Project " + proj.getProjectName() + "]");
                out.println("dir=" + proj.getRootDir());
                out.println("ear=" + proj.getEarPath());
            }
            for ( MavenDeployment depl : getDeployments() )
            {
                out.println("[Deploy " + depl.getDeploymentName() + "]");
                out.println("dir=" + depl.getDeploymentPath());
            }

            out.println("[Default]");
            out.println("skipTests=" + MavenManager.getInstance().getSkipTestsCheckBox().isSelected());
            out.println("clean=" + MavenManager.getInstance().getDoCleanCheckBox().isSelected());
            out.println("useProfile=" + MavenManager.getInstance().getUseProfileCheckBox().isSelected());
            out.println("forceUpdate=" + MavenManager.getInstance().getForceUpdateComboBox().isSelected());
            out.println(
                    "deploy=" + ((MavenDeployment) MavenManager.getInstance().getDeploymentComboBox().getSelectedItem())
                            .getDeploymentName());
            out.println("project=" + ((MavenProject) MavenManager.getInstance()
                    .getProjectComboBox()
                    .getSelectedItem()).getProjectName());
            out.println("goal=" + MavenManager.getInstance().getGoalComboBox().getSelectedItem());
            out.println("profile=" + MavenManager.getInstance().getProfileTextField().getText());
        }
        catch ( IOException e )
        {
            LogViewer.handleException(e);
        }
    }

    public void readConfig()
    {
        File f = new File("maven.config");
        getProjects().clear();
        getDeployments().clear();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String line = in.readLine();
            while ( line != null )
            {
                if ( line.startsWith("[Project") )
                {
                    String projectName = line.substring(line.indexOf(' ') + 1);
                    projectName = projectName.substring(0, projectName.length() - 1);
                    String dir = "";
                    String ear = "";
                    for ( int i = 0; i < 2; i++ )
                    {
                        line = in.readLine();
                        if ( line.startsWith("dir=") )
                        {
                            dir = line.substring(4);
                        }
                        else if ( line.startsWith("ear=") )
                        {
                            ear = line.substring(4);
                        }
                        else
                        {
                            throw new IOException("Malformed project config: " + projectName);
                        }
                    }

                    getProjects().add(new MavenProject(projectName, dir, ear));
                }
                else if ( line.startsWith("[Deploy") )
                {
                    String deploymentName = line.substring(line.indexOf(' ') + 1);
                    deploymentName = deploymentName.substring(0, deploymentName.length() - 1);
                    String dir = "";

                    line = in.readLine();
                    if ( !line.startsWith("dir=") )
                    {
                        throw new IOException("Malformed deployment config: " + deploymentName);
                    }
                    else
                    {
                        dir = line.substring(4);
                    }

                    getDeployments().add(new MavenDeployment(deploymentName, dir));
                }
                else if ( line.startsWith("[Default]") )
                {
                    String project = "";
                    String goal = "";
                    boolean tests = false;
                    boolean clean = false;
                    String profile = "";
                    boolean useProfile = false;
                    boolean forceUpdate = false;
                    String deploy = "";

                    for ( int i = 0; i < 8; i++ )
                    {
                        line = in.readLine();
                        String var = line.substring(0, line.indexOf('='));
                        String param = line.substring(var.length() + 1);
                        switch ( var )
                        {
                            case "project":
                                project = param;
                                break;
                            case "goal":
                                goal = param;
                                break;
                            case "profile":
                                profile = param;
                                break;
                            case "deploy":
                                deploy = param;
                                break;
                            case "clean":
                                clean = param.equalsIgnoreCase("true");
                                break;
                            case "useProfile":
                                useProfile = param.equalsIgnoreCase("true");
                                break;
                            case "skipTests":
                                tests = param.equalsIgnoreCase("true");
                                break;
                            case "forceUpdate":
                                forceUpdate = param.equalsIgnoreCase("true");
                                break;
                        }
                    }

                    defaultClean = clean;
                    defaultUseProfile = useProfile;
                    defaultForceUpdate = forceUpdate;
                    defaultGoal = goal;
                    defaultTests = tests;
                    defaultProject = project;
                    defaultProfile = profile;
                    defaultDeployment = deploy;
                }

                line = in.readLine();
            }
        }
        catch ( IOException e )
        {
            LogViewer.handleException(e);
        }
    }

    @Override
    public void displayEditPanel()
    {
        editPanel = new MavenEditPanel();
        new ConfigDialog(this, editPanel).setVisible(true);
    }

    @Override
    public void updateConfig()
    {
        deployments = new ArrayList<>(editPanel.getMavenDeployments());
        projects = new ArrayList<>(editPanel.getMavenProjects());

        saveMavenConfig();
        MavenManager.getInstance().init();
    }
}
