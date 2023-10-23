package scripts.inserts.model;

import java.util.List;
import model.EnsembleFamily;
import model.Model;

public class InsertModelTable {

  public static String buildInsertIntoModelSQL(Model model, List<EnsembleFamily> ensembleFamilies) {
    String advantagesArray = "{" + String.join(",", model.getMetadata().getAdvantages()) + "}";
    String disadvantagesArray =
        "{" + String.join(",", model.getMetadata().getDisadvantages()) + "}";

    String ensembleType = null;
    String familyType = null;
    for (EnsembleFamily ensembleFamily : ensembleFamilies) {
      if (ensembleFamily.getName().equals(model.getName())) {
        ensembleType = ensembleFamily.getEnsembleType();
        familyType = ensembleFamily.getFamily();
        break;
      }
    }

    return String.format(
        "INSERT INTO model(name, ml_task_id, description, display_name, structure_id, advantages, disadvantages, enabled, ensemble_type_id, family_type_id, decision_tree, model_type_id) VALUES ('%s', (select id from ml_task_type where name='%s'),'%s', '%s', (select id from model_structure_type where name='%s'), '%s', '%s', %b, (select id from model_ensemble_type where name='%s'),(select id from model_family_type where name='%s'), %b, (select id from model_type where name='%s'));\n",
        model.getName(),
        model.getMlTask(),
        model.getMetadata().getModelDescription(),
        model.getMetadata().getDisplayName(),
        model.getMetadata().getStructure(),
        advantagesArray,
        disadvantagesArray,
        !model.isBlackListed(),
        ensembleType,
        familyType,
        model.getMetadata().getSupports().getDecisionTree(),
        model.getMetadata().getModelType().get(0));
  }

  public static String buildInsertIntoModelToGroupSQL(String name, List<String> modelGroups) {
    StringBuilder sb = new StringBuilder();

    for (String group : modelGroups) {
      sb.append(
              "INSERT INTO rel_model__groups(model_id, group_id) VALUES ((select id from Model where name='")
          .append(name)
          .append("'),")
          .append("(select id from model_group_type where name='")
          .append(group)
          .append("'));\n");
    }

    return sb.toString();
  }

  public static String buildInsertIntoIncompatibleMetricSQL(Model model) {
    StringBuilder sb = new StringBuilder();
    for (String metric : model.getIncompatibleMetrics()) {
      sb.append(
              "INSERT INTO rel_model__incompatible_metrics(model_id, metric_id) VALUES ((select id from model where name='")
          .append(model.getName())
          .append("'), (select id from metric where name='")
          .append(metric)
          .append("'));\n");
    }
    return sb.toString();
  }
}
