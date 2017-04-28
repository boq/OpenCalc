package info.openmods.calc.symbol;

import info.openmods.calc.Frame;
import info.openmods.calc.utils.OptionalInt;

public interface ICallable<E> {
	public void call(Frame<E> frame, OptionalInt argumentsCount, OptionalInt returnsCount);
}