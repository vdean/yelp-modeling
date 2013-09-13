/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Yelp;
import java.util.Map;


public class JSONString implements JSONValue {
    private StringBuffer buffer = new StringBuffer();
    
    /**
     * @param value
     */
    public JSONString(final String value) {
        buffer.append(value);
    }

    /**
     * @param append
     */
    public void append(final String append) {
        buffer.append(append);
    }
    
    /**
     * @return the buffer
     */
    public StringBuffer getValue() {
        return buffer;
    }

    /**
     * (non-Javadoc)
     * @see org.apache.commons.json.JSONValue#toJSON()
     */
    public String toJSON() {
        StringBuffer result = new StringBuffer();
        result.append("\"");
        result.append(buffer);
        result.append("\"");
        return result.toString();
    }
    
    public void flattenTo(Map<String, String> map, String prefix) {
      map.put(prefix, toJSON());
    }


    /**
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return buffer.toString();
    }
}
