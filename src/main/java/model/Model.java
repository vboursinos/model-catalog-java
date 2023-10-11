package model;

import java.util.List;

public class Model {
    private String name;
    private String mlTask;
    //    private Parameter parameter;
    public Metadata metadata;


    private List<HyperParameter> hyperParameters;

    private List<ConstraintEdge> constraintEdges;
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

//    public Parameter getParameter() {
//        return parameter;
//    }
//
//    public void setParameter(Parameter parameter) {
//        this.parameter = parameter;
//    }

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

    public List<HyperParameter> getHyperParameters() {
        return hyperParameters;
    }

    public void setHyperParameters(List<HyperParameter> hyperParameters) {
        this.hyperParameters = hyperParameters;
    }

    public List<ConstraintEdge> getConstraintEdges() {
        return constraintEdges;
    }

    public void setConstraintEdges(List<ConstraintEdge> constraintEdges) {
        this.constraintEdges = constraintEdges;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
