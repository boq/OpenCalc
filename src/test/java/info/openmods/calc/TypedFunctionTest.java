package info.openmods.calc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import info.openmods.calc.symbol.ICallable;
import info.openmods.calc.types.multi.IConverter;
import info.openmods.calc.types.multi.TypeDomain;
import info.openmods.calc.types.multi.TypedFunction;
import info.openmods.calc.types.multi.TypedFunction.AmbiguousDispatchException;
import info.openmods.calc.types.multi.TypedFunction.DispatchArg;
import info.openmods.calc.types.multi.TypedFunction.DispatchException;
import info.openmods.calc.types.multi.TypedFunction.MultiReturn;
import info.openmods.calc.types.multi.TypedFunction.MultipleReturn;
import info.openmods.calc.types.multi.TypedFunction.NonCompatibleMethodsPresent;
import info.openmods.calc.types.multi.TypedFunction.NonStaticMethodsPresent;
import info.openmods.calc.types.multi.TypedFunction.OptionalArgs;
import info.openmods.calc.types.multi.TypedFunction.RawArg;
import info.openmods.calc.types.multi.TypedFunction.RawDispatchArg;
import info.openmods.calc.types.multi.TypedFunction.RawReturn;
import info.openmods.calc.types.multi.TypedFunction.Variant;
import info.openmods.calc.types.multi.TypedValue;
import info.openmods.calc.utils.OptionalInt;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class TypedFunctionTest {

	private static final TypeDomain domain = new TypeDomain();
	static {
		domain.registerType(Integer.class);
		domain.registerType(Boolean.class);
		domain.registerType(String.class);
		domain.registerType(Number.class);
		domain.registerCast(Integer.class, Number.class);
		domain.registerConverter(new IConverter<Boolean, Integer>() {
			@Override
			public Integer convert(Boolean value) {
				return value? 1 : 0;
			}
		});
	}

	private static TypedValue wrap(int v) {
		return domain.create(Integer.class, v);
	}

	private static TypedValue wrap(boolean v) {
		return domain.create(Boolean.class, v);
	}

	private static TypedValue wrap(String v) {
		return domain.create(String.class, v);
	}

	private static <T> void assertValueEquals(TypedValue value, Class<? extends T> expectedType, T expectedValue) {
		Assert.assertEquals(expectedValue, value.value);
		Assert.assertEquals(expectedType, value.type);
		Assert.assertEquals(domain, value.domain);
	}

	private static void assertValueEquals(TypedValue value, TypedValue expected) {
		assertValueEquals(value, expected.type, expected.value);
	}

	private static TypedValue execute(ICallable<TypedValue> f, TypedValue... values) {
		return execute(f, OptionalInt.of(values.length), values);
	}

	private static TypedValue execute(ICallable<TypedValue> f, OptionalInt argCount, TypedValue... values) {
		final Frame<TypedValue> frame = FrameFactory.createTopFrame();
		frame.stack().pushAll(Arrays.asList(values));
		f.call(frame, argCount, OptionalInt.absent());
		final TypedValue result = frame.stack().pop();
		Assert.assertTrue(frame.stack().isEmpty());
		return result;
	}

	private static List<TypedValue> execute(ICallable<TypedValue> f, OptionalInt argCount, int rets, TypedValue... values) {
		final Frame<TypedValue> frame = FrameFactory.createTopFrame();
		frame.stack().pushAll(Arrays.asList(values));
		f.call(frame, argCount, OptionalInt.of(rets));
		List<TypedValue> results = Lists.newArrayList();
		for (int i = 0; i < rets; i++)
			results.add(frame.stack().pop());

		Assert.assertTrue(frame.stack().isEmpty());
		return Lists.reverse(results);
	}

	private static <T> ICallable<TypedValue> createFunction(T target, Class<? extends T> cls) {
		TypedFunction.Builder builder = TypedFunction.builder();
		builder.addVariants(cls);
		return builder.build(domain, target);
	}

	@Test
	public void testSingleMethodAllMandatoryArgs() {
		abstract class Intf {
			@Variant
			public abstract Integer test(Boolean a, String b, Number c);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap(true);
		final TypedValue arg2 = wrap("Hello world");
		final TypedValue arg3 = wrap(7);
		Mockito.when(mock.test(anyBoolean(), anyString(), anyInt())).thenReturn(5);
		assertValueEquals(execute(target, arg1, arg2, arg3), Integer.class, 5);
		Mockito.verify(mock).test(true, "Hello world", 7);

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodArgCast() {
		abstract class Intf {
			@Variant
			public abstract Integer test(Integer a, Number b);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap(true);
		final TypedValue arg2 = wrap(5);
		Mockito.when(mock.test(anyInt(), any(Number.class))).thenReturn(5);
		assertValueEquals(execute(target, arg1, arg2), Integer.class, 5);
		Mockito.verify(mock).test(1, 5);

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodMandatoryRawArgs() {
		abstract class Intf {
			@Variant
			public abstract Integer test(Boolean a, @RawArg TypedValue b, Number c);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap(true);
		final TypedValue arg2 = wrap("Hello world");
		final TypedValue arg3 = wrap(7);
		Mockito.when(mock.test(anyBoolean(), any(TypedValue.class), anyInt())).thenReturn(5);
		assertValueEquals(execute(target, arg1, arg2, arg3), Integer.class, 5);
		Mockito.verify(mock).test(true, arg2, 7);

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodOptionalArgs() {
		abstract class Intf {
			@Variant
			public abstract Integer test(Boolean a, @OptionalArgs Optional<String> b, Optional<Number> c);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap(true);
		final TypedValue arg2 = wrap("Hello world");
		final TypedValue arg3 = wrap(6);
		Mockito.when(mock.test(anyBoolean(), ArgumentMatchers.<Optional<String>> any(), ArgumentMatchers.<Optional<Number>> any())).thenReturn(7);

		assertValueEquals(execute(target, arg1, arg2, arg3), Integer.class, 7);
		Mockito.verify(mock).test(true, Optional.of("Hello world"), Optional.<Number> of(6));

		assertValueEquals(execute(target, arg1, arg2), Integer.class, 7);
		Mockito.verify(mock).test(true, Optional.of("Hello world"), Optional.<Number> absent());

		assertValueEquals(execute(target, arg1), Integer.class, 7);
		Mockito.verify(mock).test(true, Optional.<String> absent(), Optional.<Number> absent());

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodOptionalRawArgs() {
		abstract class Intf {
			@Variant
			public abstract Integer test(Boolean a, @RawArg @OptionalArgs Optional<TypedValue> b, Optional<Number> c);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap(true);
		final TypedValue arg2 = wrap("Hello world");
		final TypedValue arg3 = wrap(6);
		Mockito.when(mock.test(anyBoolean(), ArgumentMatchers.<Optional<TypedValue>> any(), ArgumentMatchers.<Optional<Number>> any())).thenReturn(7);

		assertValueEquals(execute(target, arg1, arg2, arg3), Integer.class, 7);
		Mockito.verify(mock).test(true, Optional.of(arg2), Optional.<Number> of(6));

		assertValueEquals(execute(target, arg1, arg2), Integer.class, 7);
		Mockito.verify(mock).test(true, Optional.of(arg2), Optional.<Number> absent());

		assertValueEquals(execute(target, arg1), Integer.class, 7);
		Mockito.verify(mock).test(true, Optional.<TypedValue> absent(), Optional.<Number> absent());

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodAllOptionalArgs() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@OptionalArgs Optional<String> a, Optional<Number> b);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap("Hello world");
		final TypedValue arg2 = wrap(6);
		Mockito.when(mock.test(ArgumentMatchers.<Optional<String>> any(), ArgumentMatchers.<Optional<Number>> any())).thenReturn(7);

		assertValueEquals(execute(target, arg1, arg2), Integer.class, 7);
		Mockito.verify(mock).test(Optional.of("Hello world"), Optional.<Number> of(6));

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodVariadicArgs() {
		abstract class Intf {
			@Variant
			public abstract Integer test(Boolean a, Integer... bs);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap(false);
		final TypedValue arg2a = wrap(6);
		final TypedValue arg2b = wrap(7);

		Mockito.when(mock.test(anyBoolean(), ArgumentMatchers.<Integer[]> any())).thenReturn(5);

		assertValueEquals(execute(target, arg1), Integer.class, 5);
		Mockito.verify(mock).test(false);

		assertValueEquals(execute(target, arg1, arg2a), Integer.class, 5);
		Mockito.verify(mock).test(false, 6);

		assertValueEquals(execute(target, arg1, arg2a, arg2b), Integer.class, 5);
		Mockito.verify(mock).test(false, 6, 7);

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodVariadicRawArgs() {
		abstract class Intf {
			@Variant
			public abstract Integer test(Boolean a, @RawArg TypedValue... bs);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap(false);
		final TypedValue arg2a = wrap(6);
		final TypedValue arg2b = wrap(7);

		Mockito.when(mock.test(anyBoolean(), ArgumentMatchers.<TypedValue[]> any())).thenReturn(5);

		assertValueEquals(execute(target, arg1), Integer.class, 5);
		Mockito.verify(mock).test(false);

		assertValueEquals(execute(target, arg1, arg2a), Integer.class, 5);
		Mockito.verify(mock).test(false, arg2a);

		assertValueEquals(execute(target, arg1, arg2a, arg2b), Integer.class, 5);
		Mockito.verify(mock).test(false, arg2a, arg2b);

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodOptionalAndVariadicArgs() {
		abstract class Intf {
			@Variant
			public abstract Integer test(Boolean a, @OptionalArgs Optional<Number> b, Integer... cs);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue arg1 = wrap(false);
		final TypedValue arg2 = wrap(5);
		final TypedValue arg3a = wrap(6);
		final TypedValue arg3b = wrap(7);

		Mockito.when(mock.test(anyBoolean(), ArgumentMatchers.<Optional<Number>> any(), ArgumentMatchers.<Integer[]> any())).thenReturn(5);

		assertValueEquals(execute(target, arg1), Integer.class, 5);
		Mockito.verify(mock).test(false, Optional.<Number> absent());

		assertValueEquals(execute(target, arg1, arg2), Integer.class, 5);
		Mockito.verify(mock).test(false, Optional.<Number> of(5));

		assertValueEquals(execute(target, arg1, arg2, arg3a), Integer.class, 5);
		Mockito.verify(mock).test(false, Optional.<Number> of(5), 6);

		assertValueEquals(execute(target, arg1, arg2, arg3a, arg3b), Integer.class, 5);
		Mockito.verify(mock).test(false, Optional.<Number> of(5), 6, 7);

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodRawReturn() {
		abstract class Intf {
			@Variant
			@RawReturn
			public abstract TypedValue test();
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue ret = wrap("Hello world");
		Mockito.when(mock.test()).thenReturn(ret);
		assertValueEquals(execute(target, OptionalInt.absent()), ret);
		Mockito.verify(mock).test();

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodMultiReturn() {
		abstract class Intf {
			@Variant
			public abstract MultipleReturn test();
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		final TypedValue ret1 = wrap("Hello world");
		final TypedValue ret2 = wrap(7);
		Mockito.when(mock.test()).thenReturn(MultipleReturn.wrap(ret1, ret2));
		Assert.assertEquals(execute(target, OptionalInt.absent(), 2), Arrays.asList(ret1, ret2));
		Mockito.verify(mock).test();

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodMultiArrayReturn() {
		abstract class Intf {
			@Variant
			@MultiReturn
			public abstract Integer[] test();
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test()).thenReturn(new Integer[] { 3, 1, 5 });
		Assert.assertEquals(execute(target, OptionalInt.absent(), 3), Arrays.asList(wrap(3), wrap(1), wrap(5)));
		Mockito.verify(mock).test();

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleMethodMultiIterableReturn() {
		abstract class Intf {
			@Variant
			@MultiReturn
			public abstract List<String> test();
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test()).thenReturn(Lists.newArrayList("b", "c", "a", "d"));
		Assert.assertEquals(execute(target, OptionalInt.absent(), 4), Arrays.asList(wrap("b"), wrap("c"), wrap("a"), wrap("d")));
		Mockito.verify(mock).test();

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleArgumentDispatch() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@DispatchArg Integer v);

			@Variant
			public abstract String test(@DispatchArg String v);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test(anyInt())).thenReturn(7);
		Mockito.when(mock.test(anyString())).thenReturn("b");

		assertValueEquals(execute(target, wrap(6)), Integer.class, 7);
		Mockito.verify(mock).test(6);

		assertValueEquals(execute(target, wrap("a")), String.class, "b");
		Mockito.verify(mock).test("a");

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testOneDispatchOneNonDispatchArguments() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@DispatchArg Integer v, Boolean n);

			@Variant
			public abstract String test(@DispatchArg String v, Boolean n);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test(anyInt(), anyBoolean())).thenReturn(7);
		Mockito.when(mock.test(anyString(), anyBoolean())).thenReturn("b");

		assertValueEquals(execute(target, wrap(6), wrap(true)), Integer.class, 7);
		Mockito.verify(mock).test(6, true);

		assertValueEquals(execute(target, wrap("a"), wrap(true)), String.class, "b");
		Mockito.verify(mock).test("a", true);

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleArgumentDispatchWithExtraEntries() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@DispatchArg(extra = { Boolean.class }) Integer v);

			@Variant
			public abstract String test(@DispatchArg String v);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test(anyInt())).thenReturn(7);
		Mockito.when(mock.test(anyString())).thenReturn("b");

		assertValueEquals(execute(target, wrap(6)), Integer.class, 7);
		Mockito.verify(mock).test(6);

		assertValueEquals(execute(target, wrap(true)), Integer.class, 7);
		Mockito.verify(mock).test(1);

		assertValueEquals(execute(target, wrap("a")), String.class, "b");
		Mockito.verify(mock).test("a");

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testSingleRawArgumentDispatch() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@RawDispatchArg({ Integer.class, Boolean.class }) TypedValue v);

			@Variant
			public abstract String test(@DispatchArg String v);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test(any(TypedValue.class))).thenReturn(7);
		Mockito.when(mock.test(anyString())).thenReturn("b");

		assertValueEquals(execute(target, wrap(6)), Integer.class, 7);
		Mockito.verify(mock).test(wrap(6));

		assertValueEquals(execute(target, wrap(true)), Integer.class, 7);
		Mockito.verify(mock).test(wrap(true));

		assertValueEquals(execute(target, wrap("a")), String.class, "b");
		Mockito.verify(mock).test("a");

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testDoubleArgumentDispatch() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@DispatchArg Integer v1, @DispatchArg Boolean v2);

			@Variant
			public abstract String test(@DispatchArg String v, @DispatchArg Integer v2);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test(anyInt(), anyBoolean())).thenReturn(7);
		Mockito.when(mock.test(anyString(), anyInt())).thenReturn("b");

		assertValueEquals(execute(target, wrap(6), wrap(false)), Integer.class, 7);
		Mockito.verify(mock).test(6, false);

		try {
			execute(target, wrap(6), wrap(5));
			Assert.fail();
		} catch (DispatchException e) {}

		assertValueEquals(execute(target, wrap("a"), wrap(5)), String.class, "b");
		Mockito.verify(mock).test("a", 5);

		try {
			execute(target, wrap("a"), wrap(true));
			Assert.fail();
		} catch (DispatchException e) {}

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testDifferentLengthDispatch() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@DispatchArg Integer v1, @DispatchArg Integer v2);

			@Variant
			public abstract Integer test(@DispatchArg Integer v1);

			@Variant
			public abstract String test();
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test(anyInt(), anyInt())).thenReturn(2);
		Mockito.when(mock.test(anyInt())).thenReturn(1);
		Mockito.when(mock.test()).thenReturn("zero");

		assertValueEquals(execute(target, wrap(1), wrap(2)), Integer.class, 2);
		Mockito.verify(mock).test(1, 2);

		assertValueEquals(execute(target, wrap(1)), Integer.class, 1);
		Mockito.verify(mock).test(1);

		assertValueEquals(execute(target), String.class, "zero");
		Mockito.verify(mock).test();

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testOptionalArgumentDispatch() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@OptionalArgs @DispatchArg Optional<Integer> v);

			@Variant
			public abstract String test(@DispatchArg String v);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test(ArgumentMatchers.<Optional<Integer>> any())).thenReturn(7);
		Mockito.when(mock.test(anyString())).thenReturn("b");

		assertValueEquals(execute(target, wrap(6)), Integer.class, 7);
		Mockito.verify(mock).test(Optional.of(6));

		assertValueEquals(execute(target), Integer.class, 7);
		Mockito.verify(mock).test(Optional.<Integer> absent());

		assertValueEquals(execute(target, wrap("a")), String.class, "b");
		Mockito.verify(mock).test("a");

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test
	public void testDoubleArgumentDispatchMixedMatch() {
		abstract class Intf {
			@Variant
			public abstract Integer test(@DispatchArg Integer v1, @DispatchArg Boolean v2);

			@Variant
			public abstract Integer test(@DispatchArg Integer v1, @DispatchArg String v2);

			@Variant
			public abstract String test(@DispatchArg String v, @DispatchArg Integer v2);

			@Variant
			public abstract String test(@DispatchArg Boolean v, @DispatchArg Integer v2);
		}

		final Intf mock = Mockito.mock(Intf.class);
		ICallable<TypedValue> target = createFunction(mock, Intf.class);

		Mockito.when(mock.test(anyInt(), anyBoolean())).thenReturn(7);
		Mockito.when(mock.test(anyInt(), anyString())).thenReturn(8);
		Mockito.when(mock.test(anyString(), anyInt())).thenReturn("b");
		Mockito.when(mock.test(anyBoolean(), anyInt())).thenReturn("c");

		assertValueEquals(execute(target, wrap(7), wrap(false)), Integer.class, 7);
		Mockito.verify(mock).test(7, false);

		assertValueEquals(execute(target, wrap(8), wrap("c")), Integer.class, 8);
		Mockito.verify(mock).test(8, "c");

		assertValueEquals(execute(target, wrap("a"), wrap(5)), String.class, "b");
		Mockito.verify(mock).test("a", 5);

		assertValueEquals(execute(target, wrap(true), wrap(6)), String.class, "c");
		Mockito.verify(mock).test(true, 6);

		Mockito.verifyNoMoreInteractions(mock);
	}

	@Test(expected = AmbiguousDispatchException.class)
	public void testTwoMethodsNoDispatch() {
		class Intf {
			@Variant
			public Integer test(Integer v) {
				return null;
			}

			@Variant
			public Integer test(String v) {
				return null;
			}
		}

		createFunction(new Intf(), Intf.class);
	}

	@Test(expected = AmbiguousDispatchException.class)
	public void testTwoMethodsSameDispatch() {
		class Intf {
			@Variant
			public Integer test(Boolean pre, @DispatchArg Integer v, String post) {
				return null;
			}

			@Variant
			public Integer test(String pre, @DispatchArg Integer v, Integer post) {
				return null;
			}
		}

		createFunction(new Intf(), Intf.class);
	}

	@Test(expected = AmbiguousDispatchException.class)
	public void testTwoMethodsSameDispatchOneRaw() {
		class Intf {
			@Variant
			public Integer test(@DispatchArg Integer v) {
				return null;
			}

			@Variant
			public Integer test(@RawDispatchArg({ Integer.class }) TypedValue v) {
				return null;
			}
		}

		createFunction(new Intf(), Intf.class);
	}

	@Test(expected = AmbiguousDispatchException.class)
	public void testTwoMethodsSameDispatchExtraDispatch() {
		class Intf {
			@Variant
			public Integer test(Boolean pre, @DispatchArg Integer v, @DispatchArg String post) {
				return null;
			}

			@Variant
			public Integer test(@DispatchArg String pre, @DispatchArg Integer v, Integer post) {
				return null;
			}
		}

		createFunction(new Intf(), Intf.class);
	}

	@Test(expected = AmbiguousDispatchException.class)
	public void testDispatchConflictOptionalVsArgMissing() {
		class Intf {
			@Variant
			public Integer test(Boolean pre, @OptionalArgs @DispatchArg Optional<Integer> v) {
				return null;
			}

			@Variant
			public Integer test(String pre, @OptionalArgs @DispatchArg Optional<Boolean> v) {
				return null;
			}
		}

		createFunction(new Intf(), Intf.class);
	}

	@Test(expected = AmbiguousDispatchException.class)
	public void testDispatchConflictOptionalVsMandatorySameType() {
		class Intf {
			@Variant
			public Integer test(@OptionalArgs @DispatchArg Optional<Integer> v) {
				return null;
			}

			@Variant
			public Integer test(@DispatchArg Integer v) {
				return null;
			}
		}

		createFunction(new Intf(), Intf.class);
	}

	@Test(expected = AmbiguousDispatchException.class)
	public void testOptionalDispatchVsMissingArg() {
		class Intf {
			@Variant
			public Integer test(@OptionalArgs @DispatchArg Optional<Integer> v) {
				return null;
			}

			@Variant
			public Integer test() {
				return null;
			}
		}

		createFunction(new Intf(), Intf.class);
	}

	@Test(expected = RuntimeException.class)
	public void testMissingTypesOnRawDispatchArg() {
		class Intf {
			@Variant
			public Integer test(@RawDispatchArg({}) TypedValue v) {
				return null;
			}
		}

		createFunction(new Intf(), Intf.class);
	}

	private static class StaticMethodsTests {
		@Variant
		public static String test(@DispatchArg Integer v) {
			return "int";
		}

		@Variant
		public static String test(@DispatchArg Boolean v) {
			return "bool";
		}
	}

	@Test
	public void testStaticMethods() {
		TypedFunction.Builder builder = TypedFunction.builder();
		builder.addVariants(StaticMethodsTests.class);
		final ICallable<TypedValue> function = builder.build(domain, (Object)null);

		assertValueEquals(execute(function, wrap(false)), wrap("bool"));
		assertValueEquals(execute(function, wrap(3)), wrap("int"));
	}

	@Test(expected = NonStaticMethodsPresent.class)
	public void testNotStaticMethodsWithNullTarget() {
		class Intf {
			@Variant
			public String test(@DispatchArg Integer v) {
				return "int";
			}
		}

		TypedFunction.Builder builder = TypedFunction.builder();
		builder.addVariants(Intf.class);
		builder.build(domain, null);
	}

	private interface TestIntfA {
		@Variant
		public String test(@DispatchArg Integer v);
	}

	private interface TestIntfB {
		@Variant
		public String test(@DispatchArg Boolean v);
	}

	@Test
	public void testMethodsFromDifferentInterfaces() {
		class Test implements TestIntfA, TestIntfB {

			@Override
			public String test(Boolean v) {
				return "bool";
			}

			@Override
			public String test(Integer v) {
				return "int";
			}

		}
		TypedFunction.Builder builder = TypedFunction.builder();
		builder.addVariants(TestIntfA.class);
		builder.addVariants(TestIntfB.class);
		final ICallable<TypedValue> function = builder.build(domain, new Test());

		assertValueEquals(execute(function, wrap(false)), wrap("bool"));
		assertValueEquals(execute(function, wrap(3)), wrap("int"));
	}

	@Test(expected = NonCompatibleMethodsPresent.class)
	public void testIncompatibleMethodsFromDifferentInterfaces() {
		class Test implements TestIntfB {

			@Override
			public String test(Boolean v) {
				return "bool";
			}

		}
		TypedFunction.Builder builder = TypedFunction.builder();
		builder.addVariants(TestIntfA.class);
		builder.addVariants(TestIntfB.class);
		builder.build(domain, new Test());
	}
}
