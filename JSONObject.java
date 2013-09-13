/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Yelp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



public class JSONObject implements JSONValue {
    private Map<String, JSONValue> object = new HashMap<String, JSONValue>();
    
    /**
     * 
     */
    public JSONObject() {
    }
    
    /**
     * @param key
     * @param value
     */
    public void put(final String key, JSONValue value) {
        if(value == null) {
            value = JSONNull.NULL;
        }
        object.put(key, value);
    }
    
    /**
     * @return the object
     */
    public Map<String, JSONValue> getValue() {
        return object;
    }

    public String toJSON() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        Set<String> keys = object.keySet();
        Iterator<String> it = keys.iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            buffer.append("\"");
            buffer.append(key);
            buffer.append("\":");
            buffer.append(object.get(key).toJSON());
            if(it.hasNext()) {
                buffer.append(",\n");
            }
        }
        buffer.append("}");
        return buffer.toString();
    }
    public void flattenTo(Map<String, String> map, String prefix) {
      Set<String> keys = object.keySet();
      Iterator<String> it = keys.iterator();
      while(it.hasNext()) {
        String key = (String)it.next();
        String new_prefix;
        if (prefix.length() > 0) {
          new_prefix = prefix + "." + key;
        } else {
          new_prefix = key;
        }
        object.get(key).flattenTo(map, new_prefix);
      }
    }

}
