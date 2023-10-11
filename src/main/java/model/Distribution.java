package model;

public class Distribution {
    private String categoricalDistribution;
    private String floatDistribution;
    private String integerDistribution;

    public Distribution() {
    }

    public String getCategoricalDistribution() {
        return categoricalDistribution;
    }

    public void setCategoricalDistribution(String categoricalDistribution) {
        this.categoricalDistribution = categoricalDistribution;
    }

    public String getFloatDistribution() {
        return floatDistribution;
    }

    public void setFloatDistribution(String floatDistribution) {
        this.floatDistribution = floatDistribution;
    }

    public String getIntegerDistribution() {
        return integerDistribution;
    }

    public void setIntegerDistribution(String integerDistribution) {
        this.integerDistribution = integerDistribution;
    }


}
