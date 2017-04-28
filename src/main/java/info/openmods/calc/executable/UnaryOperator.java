package info.openmods.calc.executable;

import info.openmods.calc.Frame;
import info.openmods.calc.FrameFactory;
import info.openmods.calc.parsing.ast.OperatorArity;
import info.openmods.calc.symbol.SymbolMap;
import info.openmods.calc.utils.Stack;

public abstract class UnaryOperator<E> extends Operator<E> {

	private UnaryOperator(String id) {
		super(id);
	}

	public abstract static class Direct<E> extends UnaryOperator<E> {

		public Direct(String id) {
			super(id);
		}

		public abstract E execute(E value);

		@Override
		public final void execute(Frame<E> frame) {
			final Stack<E> stack = frame.stack();

			final E value = stack.pop();
			final E result = execute(value);
			stack.push(result);
		}
	}

	@Override
	public OperatorArity arity() {
		return OperatorArity.UNARY;
	}

	public abstract static class Scoped<E> extends UnaryOperator<E> {

		public Scoped(String id) {
			super(id);
		}

		public abstract E execute(SymbolMap<E> symbols, E value);

		@Override
		public final void execute(Frame<E> frame) {
			final Stack<E> stack = frame.stack();

			final E value = stack.pop();
			final E result = execute(frame.symbols(), value);
			stack.push(result);
		}
	}

	public abstract static class StackBased<E> extends UnaryOperator<E> {

		public StackBased(String id) {
			super(id);
		}

		public abstract void executeOnStack(Frame<E> frame);

		@Override
		public final void execute(Frame<E> frame) {
			final Frame<E> executionFrame = FrameFactory.newLocalFrameWithSubstack(frame, 1);
			executeOnStack(executionFrame);
			executionFrame.stack().checkSizeIsExactly(1);
		}
	}

	@Override
	public boolean isLowerPriority(Operator<E> other) {
		return false; // every other operator has lower or equal precendence
	}

	@Override
	public String toString() {
		return "UnaryOperator [" + id + "]";
	}

}
