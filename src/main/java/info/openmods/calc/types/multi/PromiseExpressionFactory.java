package info.openmods.calc.types.multi;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import info.openmods.calc.Environment;
import info.openmods.calc.Frame;
import info.openmods.calc.FrameFactory;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.SymbolCall;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.ast.IParserState;
import info.openmods.calc.parsing.ast.ISymbolCallStateTransition;
import info.openmods.calc.parsing.ast.SameStateSymbolTransition;
import info.openmods.calc.parsing.node.IExprNode;
import info.openmods.calc.parsing.node.SymbolCallNode;
import info.openmods.calc.symbol.FixedCallable;
import info.openmods.calc.symbol.SymbolMap;
import info.openmods.calc.utils.OptionalInt;
import info.openmods.calc.utils.Stack;
import java.util.List;

public class PromiseExpressionFactory {

	private final TypeDomain domain;

	public PromiseExpressionFactory(TypeDomain domain) {
		this.domain = domain;
	}

	private class DelayExprNode extends SymbolCallNode<TypedValue> {

		public DelayExprNode(List<IExprNode<TypedValue>> children) {
			super(TypedCalcConstants.SYMBOL_DELAY, children);
		}

		@Override
		public void flatten(List<IExecutable<TypedValue>> output) {
			final List<IExprNode<TypedValue>> args = ImmutableList.copyOf(getChildren());
			Preconditions.checkArgument(args.size() == 1, "'delay' expects single argument");
			output.add(Value.create(Code.flattenAndWrap(domain, args.get(0))));
			output.add(new SymbolCall<TypedValue>(symbol, 1, 1));
		}
	}

	private class DelayStateTransition extends SameStateSymbolTransition<IExprNode<TypedValue>> {

		public DelayStateTransition(IParserState<IExprNode<TypedValue>> compilerState) {
			super(compilerState);
		}

		@Override
		public IExprNode<TypedValue> createRootNode(List<IExprNode<TypedValue>> children) {
			return new DelayExprNode(children);
		}

	}

	public ISymbolCallStateTransition<IExprNode<TypedValue>> createStateTransition(IParserState<IExprNode<TypedValue>> compilerState) {
		return new DelayStateTransition(compilerState);
	}

	private static class DelayCallable extends FixedCallable<TypedValue> {

		private Optional<TypedValue> value = Optional.absent();

		private final SymbolMap<TypedValue> scope;

		private final Code code;

		public DelayCallable(SymbolMap<TypedValue> scope, Code code) {
			super(0, 1);
			this.scope = scope;
			this.code = code;
		}

		@Override
		public void call(Frame<TypedValue> frame) {
			if (!value.isPresent()) {
				final Frame<TypedValue> executionFrame = FrameFactory.newLocalFrame(scope);

				code.execute(executionFrame);

				final TypedValue result = executionFrame.stack().popAndExpectEmptyStack();
				value = Optional.of(result);
			}

			frame.stack().push(value.get());
		}

	}

	private static class DelaySymbol extends FixedCallable<TypedValue> {
		public DelaySymbol() {
			super(1, 1);
		}

		@Override
		public void call(Frame<TypedValue> frame) {
			final Stack<TypedValue> stack = frame.stack();
			final TypedValue arg = stack.pop();
			final Code code = arg.as(Code.class, "'code' argument");
			stack.push(CallableValue.wrap(arg.domain, new DelayCallable(frame.symbols(), code)));
		}
	}

	private static class ForceSymbol extends FixedCallable<TypedValue> {
		public ForceSymbol() {
			super(1, 1);
		}

		@Override
		public void call(Frame<TypedValue> frame) {
			final Stack<TypedValue> stack = frame.stack();
			final TypedValue arg = stack.pop();

			MetaObjectUtils.call(frame, arg, OptionalInt.ZERO, OptionalInt.ONE);
		}

	}

	public void registerSymbols(Environment<TypedValue> env) {
		env.setGlobalSymbol(TypedCalcConstants.SYMBOL_DELAY, new DelaySymbol());
		env.setGlobalSymbol("force", new ForceSymbol());
	}

}
