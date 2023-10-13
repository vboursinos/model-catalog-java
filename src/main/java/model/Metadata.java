package model;

import java.util.List;

public class Metadata {
    private String model;
    private String modelDescription;
    private List<String> modelType;
    private List<String> advantages;
    private List<String> disadvantages;
    private List<String> prime;
    private String displayName;
    private Support supports;
    private String structure;

    public Metadata() {
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModelDescription() {
        return modelDescription;
    }

    public void setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
    }

    public List<String> getModelType() {
        return modelType;
    }

    public void setModelType(List<String> modelType) {
        this.modelType = modelType;
    }

    public List<String> getAdvantages() {
        return advantages;
    }

    public void setAdvantages(List<String> advantages) {
        this.advantages = advantages;
    }

    public List<String> getDisadvantages() {
        return disadvantages;
    }

    public void setDisadvantages(List<String> disadvantages) {
        this.disadvantages = disadvantages;
    }

    public List<String> getPrime() {
        return prime;
    }

    public void setPrime(List<String> prime) {
        this.prime = prime;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Support getSupports() {
        return supports;
    }

    public void setSupports(Support supports) {
        this.supports = supports;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }
}
