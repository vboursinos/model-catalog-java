package model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"intervals"})
public class FloatSet {

  @JsonProperty("intervals")
  private List<Interval> intervals;

  @JsonProperty("intervals")
  public List<Interval> getIntervals() {
    return intervals;
  }

  @JsonProperty("intervals")
  public void setIntervals(List<Interval> intervals) {
    this.intervals = intervals;
  }
}
