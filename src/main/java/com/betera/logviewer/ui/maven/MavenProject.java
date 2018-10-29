package com.betera.logviewer.ui.maven;

public class MavenProject
{

    private String projectName;
    private String rootDir;
    private String earPath;

    public MavenProject(String projectName, String rootDir, String earPath)
    {
        setProjectName(projectName);
        setRootDir(rootDir);
        setEarPath(earPath);
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getRootDir()
    {
        return rootDir;
    }

    public void setRootDir(String rootDir)
    {
        this.rootDir = rootDir;
    }

    public String getEarPath()
    {
        return earPath;
    }

    public void setEarPath(String earPath)
    {
        this.earPath = earPath;
    }

    public String toString()
    {
        return getProjectName();
    }

    public MavenProject copy()
    {
        return new MavenProject(projectName, rootDir, earPath);
    }
}
