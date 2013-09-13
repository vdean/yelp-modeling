package Yelp;

import java.util.Map;

public interface JSONValue {
    /**
     * Generates the JSON-String for this value object.
     * @return the JSON-String
     */
    public String toJSON();
    public void flattenTo(Map<String, String> map, String prefix);
}
