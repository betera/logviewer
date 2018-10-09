package com.betera.logviewer.ui.maven;

public class MavenDeployment
{

    private String deploymentName;

    private String deploymentPath;

    public MavenDeployment(String deploymentName, String deploymentPath)
    {
        setDeploymentName(deploymentName);
        setDeploymentPath(deploymentPath);
    }

    public String getDeploymentName()
    {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName)
    {
        this.deploymentName = deploymentName;
    }

    public String getDeploymentPath()
    {
        return deploymentPath;
    }

    public void setDeploymentPath(String deploymentPath)
    {
        this.deploymentPath = deploymentPath;
    }

    public String toString()
    {
        return getDeploymentName();
    }
}
