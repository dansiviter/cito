package cito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public enum TestUtil { ;
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
	public static <T> T get(Object source, String name, Class<T> type) {
		try {
			final Field field = findField(source.getClass(), name, type);
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
	 * @param clazz
	 * @param name
	 * @param type
	 * @return
	 */
	public static Field findField(Class<?> clazz, String name, Class<?> type) {
		Class<?> searchType = clazz;
		while (!Object.class.equals(searchType) && searchType != null) {
			Field[] fields = searchType.getDeclaredFields();
			for (Field field : fields) {
				if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
					return field;
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
}
