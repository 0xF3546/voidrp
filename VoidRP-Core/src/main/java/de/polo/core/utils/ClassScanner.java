package de.polo.core.utils;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
import de.polo.core.handler.CommandBase;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

public class ClassScanner {

    public static Set<Class<?>> findClassesWithAnnotation(String basePackage, Class<?> annotation) {
        Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
        return reflections.getTypesAnnotatedWith((Class<java.lang.annotation.Annotation>) annotation);
    }
}

