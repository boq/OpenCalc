package info.openmods.calc;

import com.google.common.base.Optional;
import info.openmods.calc.executable.UnaryOperator;
import info.openmods.calc.types.multi.MetaObject;
import info.openmods.calc.types.multi.TypeDomain;
import info.openmods.calc.types.multi.TypedUnaryOperator;
import info.openmods.calc.types.multi.TypedUnaryOperator.IOperation;
import info.openmods.calc.types.multi.TypedUnaryOperator.ISimpleOperation;
import info.openmods.calc.types.multi.TypedValue;
import org.junit.Assert;
import org.junit.Test;

public class TypedUnaryOperatorTest {

	private static void assertValueEquals(TypedValue value, TypeDomain expectedDomain, Class<?> expectedType, Object expectedValue) {
		Assert.assertEquals(expectedValue, value.value);
		Assert.assertEquals(expectedType, value.type);
		Assert.assertEquals(expectedDomain, value.domain);
	}

	private static TypedValue execute(UnaryOperator<TypedValue> op, TypedValue value) {
		final Frame<TypedValue> frame = FrameFactory.createTopFrame();
		frame.stack().push(value);
		op.execute(frame);
		final TypedValue result = frame.stack().pop();
		Assert.assertTrue(frame.stack().isEmpty());
		return result;
	}

	@Test
	public void testTypedOperation() {
		final TypeDomain domain = new TypeDomain();
		domain.registerType(Integer.class);
		domain.registerType(Number.class);
		domain.registerType(Boolean.class);

		final int precedence = 4;
		final UnaryOperator<TypedValue> op = new TypedUnaryOperator.Builder("*", precedence)
				.registerOperation(new IOperation<Integer>() {
					@Override
					public TypedValue apply(TypeDomain domain, Integer value) {
						return domain.create(Boolean.class, Boolean.TRUE);
					}
				})
				.registerOperation(new IOperation<Number>() {
					@Override
					public TypedValue apply(TypeDomain domain, Number value) {
						return domain.create(Boolean.class, Boolean.FALSE);
					}
				})
				.build(domain);

		Assert.assertEquals(op.precedence, precedence);

		{
			final TypedValue result = execute(op, domain.create(Integer.class, 123));
			assertValueEquals(result, domain, Boolean.class, Boolean.TRUE);
		}

		{
			final TypedValue result = execute(op, domain.create(Number.class, 123));
			assertValueEquals(result, domain, Boolean.class, Boolean.FALSE);
		}
	}

	@Test
	public void testSimpleTypedOperation() {
		final TypeDomain domain = new TypeDomain();
		domain.registerType(Integer.class);
		domain.registerType(Boolean.class);

		final UnaryOperator<TypedValue> op = new TypedUnaryOperator.Builder("*", 0)
				.registerOperation(new ISimpleOperation<Integer, Boolean>() {
					@Override
					public Boolean apply(Integer value) {
						return Boolean.TRUE;
					}
				})
				.build(domain);

		final TypedValue result = execute(op, domain.create(Integer.class, 123));
		assertValueEquals(result, domain, Boolean.class, Boolean.TRUE);
	}

	@Test
	public void testDefaultOperation() {
		final TypeDomain domain = new TypeDomain();
		domain.registerType(Integer.class);
		domain.registerType(Boolean.class);

		final TypedValue value = domain.create(Integer.class, 2);

		final UnaryOperator<TypedValue> op = new TypedUnaryOperator.Builder("*", 0)
				.setDefaultOperation(new TypedUnaryOperator.IDefaultOperation() {
					@Override
					public Optional<TypedValue> apply(TypeDomain domain, TypedValue v) {
						Assert.assertEquals(v, value);
						return Optional.of(domain.create(Boolean.class, Boolean.TRUE));
					}
				})
				.build(domain);

		final TypedValue result = execute(op, value);
		assertValueEquals(result, domain, Boolean.class, Boolean.TRUE);
	}

	@Test
	public void testMetaObjectAccepting() {
		final TypeDomain domain = new TypeDomain();
		domain.registerType(Integer.class);
		domain.registerType(Boolean.class);

		final MetaObject mo = MetaObject.builder()
				.set("*", new MetaObject.SlotUnaryOp() {
					@Override
					public TypedValue op(TypedValue self, Frame<TypedValue> frame) {
						assertValueEquals(self, domain, Integer.class, 2);
						return domain.create(Boolean.class, Boolean.TRUE);
					}
				})
				.build();

		final TypedValue value = domain.create(Integer.class, 2, mo);

		final UnaryOperator<TypedValue> op = new TypedUnaryOperator.Builder("*", 0)
				.setDefaultOperation(new TypedUnaryOperator.IDefaultOperation() {
					@Override
					public Optional<TypedValue> apply(TypeDomain domain, TypedValue v) {
						throw new AssertionError("Invalid function called");
					}
				})
				.build(domain);

		final TypedValue result = execute(op, value);
		assertValueEquals(result, domain, Boolean.class, Boolean.TRUE);
	}

	@Test
	public void testMetaObjectIgnoring() {
		final TypeDomain domain = new TypeDomain();
		domain.registerType(Integer.class);
		domain.registerType(Boolean.class);

		final MetaObject mo = MetaObject.builder()
				.set("*", new MetaObject.SlotUnaryOp() {
					@Override
					public TypedValue op(TypedValue self, Frame<TypedValue> frame) {
						throw new AssertionError("Invalid function called");
					}
				})
				.build();

		final TypedValue value = domain.create(Integer.class, 2, mo);

		final UnaryOperator<TypedValue> op = new TypedUnaryOperator.Builder("*", 0)
				.setNoMetaObjectOverride()
				.setDefaultOperation(new TypedUnaryOperator.IDefaultOperation() {
					@Override
					public Optional<TypedValue> apply(TypeDomain domain, TypedValue v) {
						assertValueEquals(v, domain, Integer.class, 2);
						return Optional.of(domain.create(Boolean.class, Boolean.TRUE));

					}
				})
				.build(domain);

		final TypedValue result = execute(op, value);
		assertValueEquals(result, domain, Boolean.class, Boolean.TRUE);
	}
}
