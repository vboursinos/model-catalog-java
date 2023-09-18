package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnsembleFamily {
    @JsonProperty("model_name")
    private String name;
    @JsonProperty("family")
    private String family;
    @JsonProperty("ensembleType")
    private String ensembleType;

    public EnsembleFamily() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getEnsembleType() {
        return ensembleType;
    }

    public void setEnsembleType(String ensembleType) {
        this.ensembleType = ensembleType;
    }
}
