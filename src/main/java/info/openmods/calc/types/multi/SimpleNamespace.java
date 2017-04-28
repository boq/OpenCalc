package info.openmods.calc.types.multi;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import info.openmods.calc.Frame;
import java.util.Map;

public class SimpleNamespace {

	private final Map<String, TypedValue> values;

	public SimpleNamespace(Map<String, TypedValue> values) {
		this.values = ImmutableMap.copyOf(values);
	}

	public static void register(TypeDomain domain) {
		domain.registerType(SimpleNamespace.class, "namespace", defaultMetaObject().build());
	}

	public static MetaObject.Builder defaultMetaObject() {
		return MetaObject.builder()
				.set(new MetaObject.SlotAttr() {
					@Override
					public Optional<TypedValue> attr(TypedValue self, String key, Frame<TypedValue> frame) {
						final SimpleNamespace ns = self.as(SimpleNamespace.class);
						return Optional.fromNullable(ns.values.get(key));
					}
				})
				.set(new MetaObject.SlotDir() {
					@Override
					public Iterable<String> dir(TypedValue self, Frame<TypedValue> frame) {
						return self.as(SimpleNamespace.class).values.keySet();
					}
				});
	}

}
