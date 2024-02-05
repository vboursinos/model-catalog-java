package scripts.inserts.model;

import model.EnsembleFamily;
import model.Model;

import java.util.List;

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

    String modelInsertQuery =
        String.format(
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

//    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
//    int rev = InsertStaticTables.getRev();
//    modelInsertQuery =
//        modelInsertQuery.concat(
//            "INSERT INTO revinfo(rev, revtstmp) VALUES (" + rev + ",'" + timeStamp + "');\n");
//    modelInsertQuery =
//        modelInsertQuery.concat(
//            "INSERT INTO model_aud(id, rev, revtype) VALUES ((select id from model where name='"
//                + model.getName()
//                + "'),"
//                + rev
//                + ",0);\n");
    return modelInsertQuery;
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
