package info.openmods.calc.utils.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldAccess<T> {

	public static class FieldNotFound extends RuntimeException {
		private static final long serialVersionUID = 6119776323412256889L;

		private FieldNotFound(Class<?> cls, String name) {
			super("Field not found: " + cls + "." + name);
		}
	}

	public static class FieldAccessException extends RuntimeException {
		private static final long serialVersionUID = 3261757597754500600L;

		private static String createMessage(Field f, String action) {
			return "Failed to " + action + " field " + f;
		}

		public FieldAccessException(Field f, String action, Throwable cause) {
			super(createMessage(f, action), cause);
		}

		public FieldAccessException(Field f, String action) {
			super(createMessage(f, action));
		}

	}

	public final Field field;

	private FieldAccess(Field field) {
		this.field = field;
		field.setAccessible(true);
	}

	@SuppressWarnings("unchecked")
	public T get(Object target) {
		try {
			return (T)field.get(target);
		} catch (Throwable t) {
			throw new FieldAccessException(field, "read", t);
		}
	}

	public void set(Object target, T value) {
		try {
			field.set(target, value);
		} catch (Throwable t) {
			throw new FieldAccessException(field, "set", t);
		}
	}

	public boolean isPublic() {
		return Modifier.isPublic(field.getModifiers());
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(field.getModifiers());
	}

	public boolean isProtected() {
		return Modifier.isProtected(field.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(field.getModifiers());
	}

	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>)field.getType();
	}

	@SuppressWarnings("unchecked")
	public <C> FieldAccess<C> cast(Class<? extends C> cls) {
		final Class<?> fieldType = field.getType();
		if (!cls.isAssignableFrom(fieldType)) throw new ClassCastException(field + " cannot be used as field of type " + cls);
		return (FieldAccess<C>)this;
	}

	public static <T> FieldAccess<T> create(Class<?> cls, String name) {
		final Field f;
		try {
			f = cls.getField(name);
		} catch (Exception e) {
			throw new FieldNotFound(cls, name);
		}
		return new FieldAccess<T>(f);
	}

	public static <T> FieldAccess<T> create(Field f) {
		return new FieldAccess<T>(f);
	}
}
