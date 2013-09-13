/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Yelp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON {
	public String dateFormat() default "";
}
