package com.betera.logviewer.ui.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MavenConfigManager
{

    private static List<MavenProject> projects;

    private static List<MavenDeployment> deployments;

    private static String defaultGoal;

    private static String defaultProject;

    private static boolean defaultUseProfile;
    private static boolean defaultTests;
    private static String defaultProfile;
    private static boolean defaultClean;
    private static String defaultDeployment;
    private static boolean defaultForceUpdate;

    public static boolean isDefaultForceUpdate()
    {
        return defaultForceUpdate;
    }

    public static boolean isDefaultTests()
    {
        return defaultTests;
    }

    public static String getDefaultDeployment()
    {
        return defaultDeployment;
    }

    public static String getDefaultGoal()
    {
        return defaultGoal;
    }

    public static String getDefaultProject()
    {
        return defaultProject;
    }

    public static boolean isDefaultUseProfile()
    {
        return defaultUseProfile;
    }

    public static String getDefaultProfile()
    {
        return defaultProfile;
    }

    public static boolean isDefaultClean()
    {
        return defaultClean;
    }

    public static List<MavenProject> getProjects()
    {
        if ( projects == null )
        {
            projects = new ArrayList<>();
        }

        return projects;
    }

    public static List<MavenDeployment> getDeployments()
    {
        if ( deployments == null )
        {
            deployments = new ArrayList<>();
        }

        return deployments;
    }

    public static MavenProject getProjectByName(String aName)
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

    public static MavenDeployment getDeploymentByName(String aName)
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

    public static void readConfig()
    {
        File f = new File("maven.config");
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
            e.printStackTrace();
        }
    }

}
