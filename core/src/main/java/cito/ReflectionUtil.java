package cito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public enum ReflectionUtil { ;
	/**
	 * 
	 * @param source
	 * @param name
	 * @return
	 */
	public static <T> T get(Object source, String name) {
		return get(source, name, null);
	}

	/**
	 * 
	 * @param source
	 * @param name
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(Object source, String name, Class<T> type) {
		try {
			final Field field = findField(source.getClass(), name, type);
			if (field == null) {
				throw new IllegalArgumentException("Unable to find '" + name + "' on '" + source.getClass() + "'!");
			}
			setAccessible(field);
			return (T) field.get(source);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException(
					"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	/**
	 * 
	 * @param source
	 * @param name
	 * @param value
	 */
	public static void set(Object source, String name, Object value) {
		set(source, name, value, null);
	}

	/**
	 * 
	 * @param source
	 * @param name
	 * @param value
	 * @param type
	 */
	public static void set(Object source, String name, Object value, Class<?> type) {
		try {
			final Field field = findField(source.getClass(), name, type);
			if (field == null) {
				throw new IllegalArgumentException("Unable to find '" + name + "' on '" + source.getClass() + "'!");
			}
			setAccessible(field);
			field.set(source, value);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException(
					"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	/**
	 * 
	 * @param source
	 * @param name
	 * @param args
	 * @return
	 */
	public static <T> T invoke(Object source, String name, Object... args) {
		Class<?>[] argTypes = new Class<?>[args.length];
		for (int i = 0; i< args.length; i++) {
			argTypes[i] = args[i] == null ? Object.class : args[i].getClass();
		}

		final Method method = findMethod(source.getClass(), name, argTypes);
		if (method == null) {
			throw new IllegalArgumentException("Unable to find '" + name + "' with '" + Arrays.toString(argTypes) + "' on '" + source.getClass() + "'!");
		}
		return invoke(source, method, args);
	}

	/**
	 * 
	 * @param source
	 * @param name
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invoke(Object source, Method method, Object... args) {
		try {
			if (method == null) {
				throw new NullPointerException("Method was null!");
			}
			setAccessible(method);
			return (T) method.invoke(source, args);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new IllegalStateException(
					"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	/**
	 * 
	 * @param clazz
	 * @param name
	 * @param type
	 * @return
	 */
	public static Field findField(Class<?> clazz, String name, Class<?> type) {
		Class<?> searchType = clazz;
		while (!Object.class.equals(searchType) && searchType != null) {
			for (Field field : searchType.getDeclaredFields()) {
				if (name.equals(field.getName()) && (type == null || type.equals(field.getType()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	/**
	 * 
	 * @param clazz
	 * @param name
	 * @param params
	 * @return
	 */
	public static Method findMethod(Class<?> clazz, String name, Class<?>[] params) {
		Class<?> searchType = clazz;
		while (!Object.class.equals(searchType) && searchType != null) {
			for (Method method : searchType.getDeclaredMethods()) {
				if (name.equals(method.getName()) && Arrays.equals(params, method.getParameterTypes())) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	/**
	 * 
	 * @param field
	 */
	public static void setAccessible(Field field) {
		if ((!Modifier.isPublic(field.getModifiers()) ||
				!Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
				Modifier.isFinal(field.getModifiers())) &&
				!field.isAccessible())
		{
			field.setAccessible(true);
		}
	}

	/**
	 * 
	 * @param method
	 */
	public static void setAccessible(Method method) {
		if ((!Modifier.isPublic(method.getModifiers()) ||
				!Modifier.isPublic(method.getDeclaringClass().getModifiers()) ||
				Modifier.isFinal(method.getModifiers())) &&
				!method.isAccessible())
		{
			method.setAccessible(true);
		}
	}

	/**
	 * 
	 * @param cls
	 * @param annotation
	 * @return
	 */
	public static <A extends Annotation> A getAnnotation(Class<?> cls, Class<A> annotation) {
		return annotation.cast(cls.getAnnotation(annotation));
	}

	/**
	 * 
	 * @param obj
	 * @param annotation
	 * @return
	 */
	public static <A extends Annotation> A getAnnotation(Object obj, Class<A> annotation) {
		return getAnnotation(obj.getClass(), annotation);
	}

	/**
	 * 
	 * @param cls
	 * @param annotation
	 * @param def
	 * @return
	 */
	public static <V, A extends Annotation> V getAnnotationValue(Class<?> cls, Class<A> annotation, V def) {
		final A a = getAnnotation(cls, annotation);
		return a != null ? get(a, "value") : def;
	}

	/**
	 * 
	 * @param obj
	 * @param annotation
	 * @param def
	 * @return
	 */
	public static <V, A extends Annotation> V getAnnotationValue(Object obj, Class<A> annotation, V def) {
		return getAnnotationValue(obj.getClass(), annotation, def);
	}

	/**
	 * 
	 * @param cls
	 * @param annotation
	 * @return
	 */
	public static <V, A extends Annotation> V getAnnotationValue(Class<?> cls, Class<A> annotation) {
		return getAnnotationValue(cls,  null);
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static <V, A extends Annotation> V getAnnotationValue(Object obj, Class<A> annotation) {
		return getAnnotationValue(obj.getClass(),  null);
	}
}
