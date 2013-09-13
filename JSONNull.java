package Yelp;

import java.util.Map;

public class JSONNull implements JSONValue {
    
    public static final JSONNull NULL = new JSONNull();
    
    /**
     * 
     */
    public JSONNull() {
        // no values
    }
    
    /**
     * (non-Javadoc)
     * @see org.apache.commons.json.JSONValue#toJSON()
     */
    public String toJSON() {
        return "null";
    }
    public void flattenTo(Map<String, String> map, String prefix) {
      map.put(prefix, toJSON());
    }

}
