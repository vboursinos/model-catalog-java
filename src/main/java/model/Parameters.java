package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameters {
    public List<InputParameter> inputParameters;
    public Metadata metadata;

    public Parameters() {
    }

    public List<InputParameter> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(List<InputParameter> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
