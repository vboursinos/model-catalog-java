package model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"start", "stop"})
public class Range {

  @JsonProperty("start")
  private Integer start;

  @JsonProperty("stop")
  private Integer stop;

  @JsonProperty("start")
  public Integer getStart() {
    return start;
  }

  @JsonProperty("start")
  public void setStart(Integer start) {
    this.start = start;
  }

  @JsonProperty("stop")
  public Integer getStop() {
    return stop;
  }

  @JsonProperty("stop")
  public void setStop(Integer stop) {
    this.stop = stop;
  }
}
