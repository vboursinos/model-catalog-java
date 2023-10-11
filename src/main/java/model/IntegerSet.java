package model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import model.Range;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ranges"
})
public class IntegerSet {

    @JsonProperty("ranges")
    private List<Range> ranges;

    @JsonProperty("ranges")
    public List<Range> getRanges() {
        return ranges;
    }

    @JsonProperty("ranges")
    public void setRanges(List<Range> ranges) {
        this.ranges = ranges;
    }

}