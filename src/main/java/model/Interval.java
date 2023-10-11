package model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "left",
        "lower",
        "upper",
        "right"
})
public class Interval {

    @JsonProperty("left")
    private Boolean left;
    @JsonProperty("lower")
    private Double lower;
    @JsonProperty("upper")
    private Double upper;
    @JsonProperty("right")
    private Boolean right;

    @JsonProperty("left")
    public Boolean getLeft() {
        return left;
    }

    @JsonProperty("left")
    public void setLeft(Boolean left) {
        this.left = left;
    }

    @JsonProperty("lower")
    public Double getLower() {
        return lower;
    }

    @JsonProperty("lower")
    public void setLower(Double lower) {
        this.lower = lower;
    }

    @JsonProperty("upper")
    public Double getUpper() {
        return upper;
    }

    @JsonProperty("upper")
    public void setUpper(Double upper) {
        this.upper = upper;
    }

    @JsonProperty("right")
    public Boolean getRight() {
        return right;
    }

    @JsonProperty("right")
    public void setRight(Boolean right) {
        this.right = right;
    }
}