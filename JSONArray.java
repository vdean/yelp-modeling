package Yelp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONArray implements JSONValue {
    private List<JSONValue> values = new ArrayList<JSONValue>();

    public JSONArray() {
    }

    public void add(JSONValue value) {
        if(value == null) {
            value = JSONNull.NULL;
        }
        values.add(value);
    }
    
    /**
     * @return the values
     */
    public List<JSONValue> getValue() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(List<JSONValue> values) {
        this.values = values;
    }

    /**
     * (non-Javadoc)
     * @see org.apache.commons.json.JSONValue#toJSON()
     */
    public String toJSON() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        
        Iterator<JSONValue> it = values.iterator();
        while(it.hasNext()) {
            JSONValue value = (JSONValue)it.next();
            result.append(value.toJSON());
            if(it.hasNext()) {
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }
    
    public void flattenTo(Map<String, String> map, String prefix) {
      int index = 0;
      Iterator<JSONValue> it = values.iterator();
      while(it.hasNext()) {
          JSONValue value = (JSONValue)it.next();
          value.flattenTo(map, prefix + "[" + index + "]");
          index++; 
        }
    }


}
