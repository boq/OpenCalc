package info.openmods.calc.types.multi;

import info.openmods.calc.Frame;
import info.openmods.calc.FrameFactory;
import info.openmods.calc.symbol.SingleReturnCallable;
import info.openmods.calc.utils.OptionalInt;
import info.openmods.calc.utils.Stack;
import java.util.Iterator;

public abstract class LogicFunction extends SingleReturnCallable<TypedValue> {

	private final TypedValue nullValue;

	public LogicFunction(TypedValue nullValue) {
		this.nullValue = nullValue;
	}

	@Override
	public TypedValue call(Frame<TypedValue> frame, OptionalInt argumentsCount) {
		final int argCount = argumentsCount.or(2);

		if (argCount == 0)
			return nullValue;

		final Stack<TypedValue> args = frame.stack().substack(argCount);
		final Iterator<TypedValue> it = args.iterator();

		final Frame<TypedValue> scratchFrame = FrameFactory.newLocalFrame(frame);
		final Stack<TypedValue> scratchStack = scratchFrame.stack();

		TypedValue arg;
		do {
			execute(scratchFrame, it.next());
			arg = scratchStack.pop();
			scratchStack.checkIsEmpty();
			if (shouldReturn(scratchFrame, arg)) break;
		} while (it.hasNext());

		args.clear();

		return arg;
	}

	protected abstract boolean shouldReturn(Frame<TypedValue> scratch, TypedValue arg);

	protected abstract void execute(Frame<TypedValue> scratch, TypedValue value);

	public abstract static class Eager extends LogicFunction {

		public Eager(TypedValue nullValue) {
			super(nullValue);
		}

		@Override
		protected void execute(Frame<TypedValue> scratch, TypedValue value) {
			scratch.stack().push(value);
		}

	}

	public abstract static class Shorting extends LogicFunction {

		public Shorting(TypedValue nullValue) {
			super(nullValue);
		}

		@Override
		protected void execute(Frame<TypedValue> scratch, TypedValue value) {
			final Code code = value.as(Code.class);

			code.execute(scratch);

			scratch.stack().checkSizeIsExactly(1);
		}

	}
}
