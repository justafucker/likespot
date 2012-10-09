package models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This is a temporary solution.
 */
@Target(ElementType.FIELD)
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface NoJSON {
}
