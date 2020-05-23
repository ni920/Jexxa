package io.jexxa.core.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.Validate;

public class ClassFactory
{
    /***
     * Throw a ClassFactoryException in case an exception related to reflection occurs
     */
    static class ClassFactoryException extends RuntimeException
    {
        public ClassFactoryException(Class<?> clazz, Throwable throwable)
        {
            super("Could not create class" + clazz.getName(), throwable);
        }
    }

    protected static <T> Optional<T> newInstanceOf(Class<T> clazz)
    {
        Validate.notNull(clazz);

        var defaultConstructor = getConstructor(clazz);
        if (defaultConstructor.isPresent()) {
            try
            {
                return Optional.of(clazz.cast(defaultConstructor.get().newInstance()));
            }
            catch (ReflectiveOperationException e)
            {
                throw new ClassFactoryException(clazz, e);
            }
        }

        return Optional.empty();
    }

    protected static <T> Optional<T> newInstanceOf(Class<T> clazz, Object[] parameter)
    {
        Validate.notNull(clazz);
        Validate.notNull(parameter);

        var parameterConstructor = getConstructor(clazz, parameter);

        if (parameterConstructor.isPresent()) {
            try
            {
                return Optional.of(clazz.cast(parameterConstructor.get().newInstance(parameter)));
            }
            catch ( ReflectiveOperationException e)
            {
                throw new ClassFactoryException(clazz, e);
            }
        }

        return Optional.empty();
    }


    protected static <T> Optional<T> newInstanceOf(Class<T> interfaceType, Class<?> factory)
    {
        Validate.notNull(factory);

        var method = getFactoryMethod(factory, interfaceType);
        if (method.isPresent()) {
            try
            {
                return Optional.ofNullable(
                        interfaceType.cast(method.get().invoke(null, (Object[])null))
                );
            }
            catch (ReflectiveOperationException e)
            {
                throw new ClassFactoryException(interfaceType, e);
            }
        }

        return Optional.empty();
    }


    protected static <T> Optional<T> newInstanceOf(Class<T> interfaceType, Class<?> factory, Object[] parameters)
    {
        Validate.notNull(factory);
        Validate.notNull(interfaceType);
        Validate.notNull(parameters);

        var parameterTypes = Arrays.stream(parameters).
                map(Object::getClass).
                toArray(Class<?>[]::new);

        var method = getFactoryMethod(factory, interfaceType, parameterTypes);
        if (method.isPresent()) {
            try
            {
                return Optional.ofNullable(
                        interfaceType.cast(method.get().invoke(null, parameters))
                );
            }  catch (ReflectiveOperationException e)
            {
                throw new ClassFactoryException(interfaceType, e);
            }
        }

        return Optional.empty();
    }



    private static Optional<Constructor<?>> getConstructor(Class<?> clazz)
    {
        try {
            return Optional.of(clazz.getConstructor());
        }   catch (NoSuchMethodException | SecurityException e) {
            return Optional.empty();
        }
    }

    /**
     * This method returns a constructor that can be used to create clazz with given parameters, even if constructor offers only interfaces
     * of given parameter.
     **
     * @param clazz Class of object whose constructor is requested
     * @param parameter Object array with parameters the constructor must provide 
     * @param <T> Type of the class whose constructor is requested
     * @return A constructor or an empty optional if no constructor is available that provides given parameter
     */
    private static <T> Optional<Constructor<?>> getConstructor(Class<T> clazz, Object[] parameter)
    {
       var parameterTypes = Arrays.stream(parameter)
               .map(Object::getClass)
               .toArray(Class<?>[]::new);

       return  Arrays.stream(clazz.getConstructors())
               .filter( element -> element.getParameterTypes().length == parameter.length)
               .filter( element -> isAssignableFrom(element.getParameterTypes(), parameterTypes ))
               .findFirst();
    }

    private static boolean isAssignableFrom( Class<?>[] interfaceList, Class<?>[] implementationList )
    {
        if (interfaceList.length != implementationList.length)
        {
            return false;
        }

        final AtomicInteger counter = new AtomicInteger(); // int is not possible because elements used in streams should be final
        return Arrays.stream(interfaceList).allMatch( element -> element.isAssignableFrom(implementationList[counter.getAndIncrement()]));
    }


    private static <T> Optional<Method> getFactoryMethod(Class<?> implementation, Class<T> interfaceType)
    {
        //Lookup factory method with no attributes and return type clazz
        return Arrays.stream(implementation.getMethods())
                .filter( element -> Modifier.isStatic(element.getModifiers()))
                .filter( element -> element.getParameterTypes().length == 0)
                .filter( element -> element.getReturnType().equals(interfaceType))
                .findFirst();
    }
    

    private static <T> Optional<Method> getFactoryMethod(Class<?> implementation, Class<T> interfaceType, Class<?>[] parameterTypes)
    {
        //Lookup factory method with no attributes and return type clazz
        return Arrays.stream(implementation.getMethods())
                .filter( element -> Modifier.isStatic(element.getModifiers()))
                .filter( element -> element.getParameterTypes().length == 1)
                .filter( element -> isAssignableFrom(element.getParameterTypes(), parameterTypes ))
                .filter( element -> element.getReturnType().equals(interfaceType))
                .findFirst();
    }


    private ClassFactory()
    {

    }
}
