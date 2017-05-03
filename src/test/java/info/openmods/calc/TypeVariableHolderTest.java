package info.openmods.calc;

import info.openmods.calc.utils.reflection.TypeVariableHolder;
import info.openmods.calc.utils.reflection.TypeVariableHolderFiller;
import java.lang.reflect.TypeVariable;
import org.junit.Assert;
import org.junit.Test;

public class TypeVariableHolderTest {

	public interface ParA<A, B> {}

	public interface ParB<A, B> {}

	@TypeVariableHolder(ParA.class)
	private static class ParHolder {
		public static TypeVariable<?> A;

		@TypeVariableHolder(ParB.class)
		public static TypeVariable<?> B;
	}

	@TypeVariableHolder
	private static class ParamerizedHolderClassLevel<A, B> {
		public static TypeVariable<?> A;

		public static TypeVariable<?> B;
	}

	private static class ParamerizedHolderVariableLevel<A, B> {
		@TypeVariableHolder
		public static TypeVariable<?> A;

		public static TypeVariable<?> B;
	}

	@Test
	public void testClassLevelOwnParameters() {
		TypeVariableHolderFiller.instance.initialize(ParamerizedHolderClassLevel.class);

		final TypeVariable<?>[] parameters = ParamerizedHolderClassLevel.class.getTypeParameters();
		Assert.assertEquals(parameters[0], ParamerizedHolderClassLevel.A);
		Assert.assertEquals(parameters[1], ParamerizedHolderClassLevel.B);
	}

	@Test
	public void testVariableLevelOwnParameters() {
		TypeVariableHolderFiller.instance.initialize(ParamerizedHolderVariableLevel.class);

		final TypeVariable<?>[] parameters = ParamerizedHolderVariableLevel.class.getTypeParameters();
		Assert.assertEquals(parameters[0], ParamerizedHolderVariableLevel.A);
		Assert.assertNull(ParamerizedHolderVariableLevel.B);
	}

	@Test
	public void testParametersFromExternalClass() {
		TypeVariableHolderFiller.instance.initialize(ParHolder.class);

		final TypeVariable<?>[] parametersA = ParA.class.getTypeParameters();
		final TypeVariable<?>[] parametersB = ParB.class.getTypeParameters();
		Assert.assertEquals(parametersA[0], ParHolder.A);
		Assert.assertEquals(parametersB[1], ParHolder.B);
	}
}
