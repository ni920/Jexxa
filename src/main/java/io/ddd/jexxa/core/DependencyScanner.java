package io.ddd.jexxa.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import io.github.classgraph.ClassGraph;
import org.apache.commons.lang.Validate;

public class DependencyScanner
{

    public List<Class<?>> getClassesWithAnnotation(final Class<? extends Annotation> annotation)
    {
        validateRetentionRuntime(annotation);
        return new ClassGraph()
                //.verbose()
                .enableAllInfo()
                .scan()
                .getClassesWithAnnotation(annotation.getName())
                .loadClasses();
    }

    public List<Class<?>> getClassesWithAnnotation(final Class<? extends Annotation> annotation, String packageName)
    {
        Validate.notNull(packageName);
        validateRetentionRuntime(annotation);
        return new ClassGraph()
                //.verbose()
                .enableAllInfo()
                .whitelistPackages(packageName)
                .scan()
                .getClassesWithAnnotation(annotation.getName())
                .loadClasses();
    }

    public List<Class<?>> getClassesImplementing(final Class<?> interfaceType)
    {
        Validate.notNull(interfaceType);
        return new ClassGraph()
                //.verbose()
                .enableAllInfo()
                .scan()
                .getClassesImplementing(interfaceType.getName())
                .loadClasses();
        
    }

    public List<Class<?>> getClassesImplementing(final Class<?> interfaceType, String packageName)
    {
        Validate.notNull(interfaceType);
        Validate.notNull(packageName);
        return new ClassGraph()
                //.verbose()
                .enableAllInfo()
                .whitelistPackages(packageName)
                .scan()
                .getClassesImplementing(interfaceType.getName())
                .loadClasses();

    }


    private void validateRetentionRuntime(final Class<? extends Annotation> annotation) {
        Validate.notNull(annotation.getAnnotation(Retention.class), "Annotation must be declared with '@Retention(RUNTIME)'" );
        Validate.isTrue(annotation.getAnnotation(Retention.class).value().equals(RetentionPolicy.RUNTIME), "Annotation must be declared with '@Retention(RUNTIME)");
    }

}