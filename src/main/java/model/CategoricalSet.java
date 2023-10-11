package model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "categories"
})

public class CategoricalSet {
    @JsonProperty("categories")
    private List<Object> categories;

    @JsonProperty("categories")
    public List<Object> getCategories() {
        return categories;
    }

    @JsonProperty("categories")
    public void setCategories(List<Object> categories) {
        this.categories = categories;
    }


/*    Object category = domain.getCategories().get(index);

    if (category instanceof String) {
        String strCategory = (String) category;
        // use strCategory
    } else if (category instanceof Boolean) {
        Boolean boolCategory = (Boolean) category;
        // use boolCategory
    }*/
}