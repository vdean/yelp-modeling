package Yelp;

import java.math.BigDecimal;
import java.util.Map;

public class JSONNumber implements JSONValue {
    private String value = "0";
    /**
     * 
     */
    public JSONNumber(double value) {
       this.value = Double.toString(value); 
    }
    
    /**
     * @param value
     */
    public JSONNumber(String value) {
        this.value = value;
    }
    
    /**
     * @param value
     */
    public JSONNumber(long value) {
        this.value = Long.toString(value);
    }
    
    /**
     * @param value
     */
    public JSONNumber(float value) {
        this.value = Float.toString(value);
    }

    /**
     * @param value
     */
    public JSONNumber(BigDecimal value) {
        this.value = value.toPlainString(); 
    }
    
    public JSONNumber(int value) {
        this.value = Integer.toString(value);
    }
    
    
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * (non-Javadoc)
     * @see org.apache.commons.json.JSONValue#toJSON()
     */
    public String toJSON() {
        return value;
    }
    public void flattenTo(Map<String, String> map, String prefix) {
      map.put(prefix, toJSON());
    }


}
