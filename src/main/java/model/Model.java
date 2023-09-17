package model;

import java.util.List;

public class Model {
    private String name;
    private String mlTask;
    private Parameters parameters;

    private List<String> incompatibleMetrics;
    private List<String> groups;
    private boolean blackListed;

    public Model() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMlTask() {
        return mlTask;
    }

    public void setMlTask(String mlTask) {
        this.mlTask = mlTask;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public List<String> getIncompatibleMetrics() {
        return incompatibleMetrics;
    }

    public void setIncompatibleMetrics(List<String> incompatibleMetrics) {
        this.incompatibleMetrics = incompatibleMetrics;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public boolean isBlackListed() {
        return blackListed;
    }

    public void setBlackListed(boolean blackListed) {
        this.blackListed = blackListed;
    }
}
