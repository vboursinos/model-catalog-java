package model;

import java.util.List;

public class InputParameter {
  private String parameterName;
  private String parameterType;
  private int minValue;
  private int maxValue;
  private List<String> values;
  private String defaultValue;
  private String label;
  private String description;
  private boolean enabled;
  private boolean constraint;
  private String constraintInformation;
  private boolean fixedValue;

  public InputParameter() {}

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public String getParameterType() {
    return parameterType;
  }

  public void setParameterType(String parameterType) {
    this.parameterType = parameterType;
  }

  public int getMinValue() {
    return minValue;
  }

  public void setMinValue(int minValue) {
    this.minValue = minValue;
  }

  public int getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(int maxValue) {
    this.maxValue = maxValue;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isConstraint() {
    return constraint;
  }

  public void setConstraint(boolean constraint) {
    this.constraint = constraint;
  }

  public String getConstraintInformation() {
    return constraintInformation;
  }

  public void setConstraintInformation(String constraintInformation) {
    this.constraintInformation = constraintInformation;
  }

  public boolean isFixedValue() {
    return fixedValue;
  }

  public void setFixedValue(boolean fixedValue) {
    this.fixedValue = fixedValue;
  }
}
