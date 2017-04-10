/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.annotation.Nonnull;

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
	public static <T> T get(@Nonnull Object source, @Nonnull String name) {
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
	public static <T> T get(@Nonnull Object source, @Nonnull String name, Class<T> type) {
		try {
			final Field field = findField(source.getClass(), name, type);
			if (field == null) {
				throw new IllegalArgumentException(
						String.format("Unable to find '%s' on '%s'!", name, source.getClass()));
			}
			setAccessible(field);
			return (T) field.get(source);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException(String.format(
					"Unexpected reflection exception - %s", ex.getClass().getName()), ex);
		}
	}

	/**
	 * 
	 * @param source
	 * @param name
	 * @param value
	 */
	public static void set(@Nonnull Object source, @Nonnull String name, Object value) {
		set(source, name, value, null);
	}

	/**
	 * 
	 * @param source
	 * @param name
	 * @param value
	 * @param type
	 */
	public static void set(@Nonnull Object source, @Nonnull String name, Object value, Class<?> type) {
		try {
			final Field field = findField(source.getClass(), name, type);
			if (field == null) {
				throw new IllegalArgumentException("Unable to find '" + name + "' on '" + source.getClass() + "'!");
			}
			setAccessible(field);
			field.set(source, value);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException(String.format(
					"Unexpected reflection exception - %s", ex.getClass().getName()), ex);
		}
	}

	/**
	 * 
	 * @param source
	 * @param name
	 * @param args
	 * @return
	 */
	public static <T> T invoke(@Nonnull Object source, @Nonnull String name, Object... args) {
		Class<?>[] argTypes = new Class<?>[args.length];
		for (int i = 0; i< args.length; i++) {
			argTypes[i] = args[i] == null ? Object.class : args[i].getClass();
		}

		final Method method = findMethod(source.getClass(), name, argTypes);
		if (method == null) {
			throw new IllegalArgumentException(
					String.format("Unable to find '%s' with '%s' on '%s'!",
							name, Arrays.toString(argTypes), source.getClass()));
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
	public static <T> T invoke(@Nonnull Object source, @Nonnull Method method, Object... args) {
		try {
			setAccessible(method);
			return (T) method.invoke(source, args);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new IllegalStateException(String.format(
					"Unexpected reflection exception - %s", ex.getClass().getName()), ex);
	}
	}

	/**
	 * 
	 * @param clazz
	 * @param name
	 * @param type
	 * @return
	 */
	public static Field findField(@Nonnull Class<?> clazz, @Nonnull String name, Class<?> type) {
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
	public static Method findMethod(@Nonnull Class<?> clazz, @Nonnull String name, Class<?>... params) {
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
	public static void setAccessible(@Nonnull Field field) {
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
	public static void setAccessible(@Nonnull Method method) {
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
	public static <A extends Annotation> A getAnnotation(@Nonnull Class<?> cls, @Nonnull Class<A> annotation) {
		return annotation.cast(cls.getAnnotation(annotation));
	}

	/**
	 * 
	 * @param obj
	 * @param annotation
	 * @return
	 */
	public static <A extends Annotation> A getAnnotation(@Nonnull Object obj, @Nonnull Class<A> annotation) {
		return getAnnotation(obj.getClass(), annotation);
	}

	/**
	 * 
	 * @param cls
	 * @param annotation
	 * @param def
	 * @return
	 */
	public static <V, A extends Annotation> V getAnnotationValue(
			@Nonnull Class<?> cls, @Nonnull Class<A> annotation, V def)
	{
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
	public static <V, A extends Annotation> V getAnnotationValue(
			@Nonnull Object obj, @Nonnull Class<A> annotation, V def)
	{
		return getAnnotationValue(obj.getClass(), annotation, def);
	}

	/**
	 * 
	 * @param cls
	 * @param annotation
	 * @return
	 */
	public static <V, A extends Annotation> V getAnnotationValue(@Nonnull Class<?> cls, @Nonnull Class<A> annotation) {
		return getAnnotationValue(cls, annotation, null);
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static <V, A extends Annotation> V getAnnotationValue(Object obj, Class<A> annotation) {
		return getAnnotationValue(obj.getClass(), annotation);
	}
}
