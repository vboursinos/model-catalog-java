package model;

public class Support {
   private boolean probabilities;
   private boolean featureImportances;
   private boolean decisionTree;

    public Support() {
    }

   public boolean getProbabilities() {
      return probabilities;
   }

   public void setProbabilities(boolean probabilities) {
      this.probabilities = probabilities;
   }

   public boolean getFeatureImportances() {
      return featureImportances;
   }

   public void setFeatureImportances(boolean featureImportances) {
      this.featureImportances = featureImportances;
   }

   public boolean getDecisionTree() {
      return decisionTree;
   }

   public void setDecisionTree(boolean decisionTree) {
      this.decisionTree = decisionTree;
   }
}
