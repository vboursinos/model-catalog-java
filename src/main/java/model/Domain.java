package model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "floatSet",
        "integerSet",
        "categoricalSet",
})
public class Domain {
    @JsonProperty("floatSet")
    private FloatSet floatSet;
    @JsonProperty("integerSet")
    private IntegerSet integerSet;
    @JsonProperty("categoricalSet")
    private CategoricalSet categoricalSet;

    @JsonProperty("floatSet")
    public FloatSet getFloatSet() {
        return floatSet;
    }

    @JsonProperty("floatSet")
    public void setFloatSet(FloatSet floatSet) {
        this.floatSet = floatSet;
    }

    @JsonProperty("integerSet")
    public IntegerSet getIntegerSet() {
        return integerSet;
    }

    @JsonProperty("integerSet")
    public void setIntegerSet(IntegerSet integerSet) {
        this.integerSet = integerSet;
    }

    @JsonProperty("categoricalSet")
    public CategoricalSet getCategoricalSet() {
        return categoricalSet;
    }

    @JsonProperty("categoricalSet")
    public void setCategoricalSet(CategoricalSet categoricalSet) {
        this.categoricalSet = categoricalSet;
    }
}