package info.openmods.calc.types.multi;

import com.google.common.base.Joiner;
import info.openmods.calc.Compilers;
import info.openmods.calc.ExprType;
import info.openmods.calc.Frame;
import info.openmods.calc.FrameFactory;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.symbol.ICallable;
import info.openmods.calc.utils.OptionalInt;
import info.openmods.calc.utils.Stack;
import java.util.Locale;

public class EvalSymbol implements ICallable<TypedValue> {

	private final Compilers<TypedValue, ExprType> compilers;

	public EvalSymbol(Compilers<TypedValue, ExprType> compilers) {
		this.compilers = compilers;
	}

	@Override
	public void call(Frame<TypedValue> frame, OptionalInt argumentsCount, OptionalInt returnsCount) {
		TypedCalcUtils.expectExactArgCount(argumentsCount, 2);

		final Stack<TypedValue> stack = frame.stack();
		final String code = stack.pop().as(String.class, "second 'eval' argument");
		final String type = stack.pop().as(String.class, "first 'eval' argument");

		final ExprType exprType;
		try {
			exprType = ExprType.valueOf(type.toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Value '%s' is not valid expression type, expected one of %s", type, Joiner.on(',').join(ExprType.values())));
		}

		final IExecutable<TypedValue> compiled = compilers.compile(exprType, code);

		final Frame<TypedValue> executionFrame = FrameFactory.newLocalFrameWithSubstack(frame, 0);
		compiled.execute(executionFrame);

		TypedCalcUtils.expectExactReturnCount(returnsCount, executionFrame.stack().size());
	}

}
