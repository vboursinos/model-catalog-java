public class ParameterTypeDistribution {
    private String parameterType;

    private String parameterDistribution;

    public ParameterTypeDistribution() {
    }


    public ParameterTypeDistribution(String parameterType, String parameterDistribution) {
        this.parameterType = parameterType;
        this.parameterDistribution = parameterDistribution;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getParameterDistribution() {
        return parameterDistribution;
    }

    public void setParameterDistribution(String parameterDistribution) {
        this.parameterDistribution = parameterDistribution;
    }
}
