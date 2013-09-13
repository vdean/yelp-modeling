/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Yelp;

/**
 * 
 */
public class JSONException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 4263636352860864719L;

    /**
     * 
     */
    public JSONException() {
    }

    /**
     * @param message
     */
    public JSONException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public JSONException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
