package model;

import java.util.List;

public class ConstraintEdge {
    private String source;
    private String target;
    private List<Item> mapping;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<Item> getMapping() {
        return mapping;
    }

    public void setMapping(List<Item> mapping) {
        this.mapping = mapping;
    }
}
