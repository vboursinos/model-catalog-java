package model;

public class Support {
   private boolean probabilities;
   private boolean featureImportance;
   private boolean decisionTree;

    public Support() {
    }

   public boolean getProbabilities() {
      return probabilities;
   }

   public void setProbabilities(boolean probabilities) {
      this.probabilities = probabilities;
   }

   public boolean getFeatureImportance() {
      return featureImportance;
   }

   public void setFeatureImportance(boolean featureImportance) {
      this.featureImportance = featureImportance;
   }

   public boolean getDecisionTree() {
      return decisionTree;
   }

   public void setDecisionTree(boolean decisionTree) {
      this.decisionTree = decisionTree;
   }
}
