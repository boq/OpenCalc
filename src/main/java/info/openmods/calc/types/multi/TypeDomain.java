package info.openmods.calc.types.multi;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;
import info.openmods.calc.utils.reflection.TypeVariableHolder;
import info.openmods.calc.utils.reflection.TypeVariableHolderFiller;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public class TypeDomain {

	private interface RawConverter {
		public Object convert(Object value);
	}

	private static class TypeVariableHolders {
		@TypeVariableHolder(IConverter.class)
		public static class Converter {
			public static TypeVariable<?> S;
			public static TypeVariable<?> T;
		}
	}

	static {
		TypeVariableHolderFiller.instance.initialize(TypeVariableHolders.class);
	}

	private static class WrappedConverter<S, T> implements RawConverter {
		private final Class<? extends S> source;

		private final IConverter<S, T> converter;

		public WrappedConverter(Class<? extends S> source, IConverter<S, T> converter) {
			this.source = source;
			this.converter = converter;
		}

		@Override
		public Object convert(Object value) {
			S input = source.cast(value);
			T result = converter.convert(input);
			return result;
		}
	}

	private static class CastConverter<T> implements RawConverter {
		private final Class<? extends T> target;

		public CastConverter(Class<? extends T> target) {
			this.target = target;
		}

		@Override
		public Object convert(Object value) {
			return target.cast(value);
		}

	}

	private final MetaObject defaultMetaObject;

	public TypeDomain() {
		this.defaultMetaObject = MetaObject.builder().build();
	}

	public TypeDomain(MetaObject defaultMetaObject) {
		this.defaultMetaObject = defaultMetaObject;
	}

	private static class TypeInfo {
		public final String name;
		public final MetaObject defaultMetaObject;
		public final TypedValue defaultValue;

		public TypeInfo(String name, MetaObject defaultMetaObject, TypedValue defaultValue) {
			this.name = name;
			this.defaultMetaObject = defaultMetaObject;
			this.defaultValue = defaultValue;
		}

	}

	private final Map<Class<?>, TypeInfo> allowedTypes = Maps.newIdentityHashMap();

	private final Table<Class<?>, Class<?>, RawConverter> converters = HashBasedTable.create();

	public TypeDomain registerType(Class<?> type) {
		return registerType(type, type.getSimpleName());
	}

	public TypeDomain registerType(Class<?> type, String shortName) {
		return registerType(type, shortName, defaultMetaObject);
	}

	public TypeDomain registerType(Class<?> type, String shortName, MetaObject defaultMetaObject) {
		return registerType(type, shortName, defaultMetaObject, null);
	}

	public <T> TypeDomain registerType(Class<T> type, String shortName, MetaObject defaultMetaObject, T defaultValue) {
		if (defaultValue == null) {
			allowedTypes.put(type, new TypeInfo(shortName, defaultMetaObject, null));
		} else {
			final TypedValue defaultWrappedValue = new TypedValue(this, type, defaultValue);
			allowedTypes.put(type, new TypeInfo(shortName, defaultMetaObject, defaultWrappedValue));
		}
		return this;
	}

	public boolean isKnownType(Class<?> type) {
		return allowedTypes.containsKey(type);
	}

	public void checkIsKnownType(Class<?> type) {
		Preconditions.checkState(allowedTypes.containsKey(type), "Type '%s' is not allowed in domain", type);
	}

	public String getName(Class<?> type) {
		final TypeInfo typeInfo = allowedTypes.get(type);
		Preconditions.checkState(typeInfo != null, "Type %s is not registered", type);
		return typeInfo.name;
	}

	public Optional<String> tryGetName(Class<?> type) {
		final TypeInfo typeInfo = allowedTypes.get(type);
		if (typeInfo == null) return Optional.absent();
		return Optional.of(typeInfo.name);
	}

	public MetaObject getDefaultMetaObject(Class<?> type) {
		final TypeInfo typeInfo = allowedTypes.get(type);
		Preconditions.checkState(typeInfo != null, "Type %s is not registered", type);
		return typeInfo.defaultMetaObject;
	}

	public <T> TypeDomain registerCast(Class<? extends T> source, Class<T> target) {
		checkIsKnownType(source);
		checkIsKnownType(target);
		final RawConverter prev = converters.put(source, target, new CastConverter<T>(target));
		Preconditions.checkState(prev == null, "Duplicate registration for types (%s,%s)", source, target);
		return this;
	}

	public <S, T> TypeDomain registerConverter(Class<? extends S> source, Class<? extends T> target, IConverter<S, T> converter) {
		checkIsKnownType(source);
		checkIsKnownType(target);
		final RawConverter prev = converters.put(source, target, new WrappedConverter<S, T>(source, converter));
		Preconditions.checkState(prev == null, "Duplicate registration for types (%s,%s)", source, target);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <S, T> TypeDomain registerConverter(IConverter<S, T> converter) {
		final TypeToken<?> converterType = TypeToken.of(converter.getClass());
		final Class<S> sourceType = (Class<S>)converterType.resolveType(TypeVariableHolders.Converter.S).getRawType();
		final Class<T> targetType = (Class<T>)converterType.resolveType(TypeVariableHolders.Converter.T).getRawType();
		return registerConverter(sourceType, targetType, converter);
	}

	private RawConverter getConverter(TypedValue value, Class<?> type) {
		final RawConverter converter = converters.get(value.type, type);
		Preconditions.checkArgument(converter != null, "No known conversion from %s to %s", value.type, type);
		return converter;
	}

	public boolean hasConversion(Class<?> from, Class<?> to) {
		return converters.contains(from, to);
	}

	public void checkConversion(Class<?> from, Class<?> to) {
		Preconditions.checkArgument(hasConversion(from, to), "No known conversion from %s to %s", from, to);
	}

	public TypedValue convert(TypedValue value, Class<?> type) {
		Preconditions.checkArgument(value.domain == this, "Mixed domain");
		if (value.type == type) return value;
		final RawConverter converter = getConverter(value, type);
		final Object convertedValue = converter.convert(value.value);
		return new TypedValue(this, type, convertedValue);
	}

	public <T> T unwrap(TypedValue value, Class<T> type) {
		Preconditions.checkArgument(value.domain == this, "Mixed domain");
		if (value.type == type) return value.as(type);
		final RawConverter converter = getConverter(value, type);
		final Object convertedValue = converter.convert(value.value);
		return type.cast(convertedValue);
	}

	public enum Coercion {
		TO_LEFT, TO_RIGHT, INVALID;
	}

	private static final Map<Coercion, Coercion> inverses = Maps.newEnumMap(Coercion.class);

	static {
		inverses.put(Coercion.TO_LEFT, Coercion.TO_RIGHT);
		inverses.put(Coercion.TO_RIGHT, Coercion.TO_LEFT);
		inverses.put(Coercion.INVALID, Coercion.INVALID);
	}

	private final Table<Class<?>, Class<?>, Coercion> coercionRules = HashBasedTable.create();

	public TypeDomain registerCoercionRule(Class<?> left, Class<?> right, Coercion rule) {
		checkIsKnownType(left);
		checkIsKnownType(right);
		Preconditions.checkArgument(left != right);
		if (rule == Coercion.TO_LEFT) {
			checkConversion(right, left);
		} else if (rule == Coercion.TO_RIGHT) {
			checkConversion(left, right);
		}

		final Coercion prev = coercionRules.put(left, right, rule);
		Preconditions.checkState(prev == null || prev == rule, "Duplicate coercion rule for (%s,%s): %s -> %s", left, right, rule);
		return this;
	}

	public TypeDomain registerSymmetricCoercionRule(Class<?> left, Class<?> right, Coercion rule) {
		registerCoercionRule(left, right, rule);
		registerCoercionRule(right, left, inverses.get(rule));
		return this;
	}

	public Coercion getCoercionRule(Class<?> left, Class<?> right) {
		if (left == right) return Coercion.TO_LEFT;
		final Coercion result = coercionRules.get(left, right);
		return result != null? result : Coercion.INVALID;
	}

	public <T> TypedValue getDefault(Class<T> type) {
		final TypeInfo typeInfo = allowedTypes.get(type);
		Preconditions.checkState(typeInfo != null, "Type '%s' is not allowed in domain", type);
		Preconditions.checkState(typeInfo.defaultValue != null, "Type %s has no default value");
		return typeInfo.defaultValue;
	}

	public <T> TypedValue create(Class<T> type, T value) {
		checkIsKnownType(type);
		return new TypedValue(this, type, value);
	}

	public <T> TypedValue create(Class<T> type, T value, MetaObject metaObject) {
		checkIsKnownType(type);
		return new TypedValue(this, type, value, metaObject);
	}

	public <T> TypedValue castAndCreate(Class<T> type, Object value) {
		return create(type, type.cast(value));
	}

	public <T> Function<T, TypedValue> createWrappingTransformer(final Class<T> type) {
		checkIsKnownType(type);
		return input -> create(type, input);
	}

	public <T> Function<T, TypedValue> createWrappingTransformer(final Class<T> type, final TypedValue nullValue) {
		checkIsKnownType(type);
		Preconditions.checkArgument(nullValue.domain == this, "Different domains");
		return input -> input != null? create(type, input) : nullValue;
	}

	public <T> Function<TypedValue, T> createUnwrappingTransformer(final Class<T> type) {
		checkIsKnownType(type);
		return input -> input.as(type);
	}

	public <T> Function<TypedValue, T> createUnwrappingTransformer(final Class<T> type, final TypedValue nullValue) {
		checkIsKnownType(type);
		Preconditions.checkArgument(nullValue.domain == this, "Different domains");
		return input -> !nullValue.equals(input)? input.as(type) : null;
	}
}
