package info.openmods.calc.symbol;

import info.openmods.calc.Frame;
import info.openmods.calc.types.multi.TypedCalcUtils;
import info.openmods.calc.utils.OptionalInt;

public abstract class SingleReturnCallable<E> implements ICallable<E> {

	@Override
	public final void call(Frame<E> frame, OptionalInt argumentsCount, OptionalInt returnsCount) {
		TypedCalcUtils.expectSingleReturn(returnsCount);

		final E result = call(frame, argumentsCount);
		frame.stack().push(result);
	}

	public abstract E call(Frame<E> frame, OptionalInt argumentsCount);
}
