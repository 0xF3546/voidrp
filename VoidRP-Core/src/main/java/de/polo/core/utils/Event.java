package de.polo.core.utils;

import java.lang.annotation.*;

/**
 * Annotation, um Event-Klassen zu markieren.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Event {
}
