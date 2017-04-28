package info.openmods.calc.types.multi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import info.openmods.calc.Environment;
import info.openmods.calc.Frame;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.SymbolCall;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.ast.IParserState;
import info.openmods.calc.parsing.ast.ISymbolCallStateTransition;
import info.openmods.calc.parsing.ast.SameStateSymbolTransition;
import info.openmods.calc.parsing.node.IExprNode;
import info.openmods.calc.parsing.node.SymbolCallNode;
import info.openmods.calc.symbol.FixedCallable;
import java.util.List;

public class IfExpressionFactory {

	private final TypeDomain domain;

	public IfExpressionFactory(TypeDomain domain) {
		this.domain = domain;
	}

	private class IfNode extends SymbolCallNode<TypedValue> {
		public IfNode(List<IExprNode<TypedValue>> args) {
			super(TypedCalcConstants.SYMBOL_IF, args);
		}

		@Override
		public void flatten(List<IExecutable<TypedValue>> output) {
			final List<IExprNode<TypedValue>> args = ImmutableList.copyOf(getChildren());
			Preconditions.checkState(args.size() == 3, "Expected 3 parameter for 'if', got %s", args.size());
			final IExprNode<TypedValue> condition = args.get(0);
			final IExprNode<TypedValue> ifTrue = args.get(1);
			final IExprNode<TypedValue> ifFalse = args.get(2);

			condition.flatten(output);
			output.add(Value.create(Code.flattenAndWrap(domain, ifTrue)));
			output.add(Value.create(Code.flattenAndWrap(domain, ifFalse)));
			output.add(new SymbolCall<TypedValue>(TypedCalcConstants.SYMBOL_IF, 3, 1));
		}

	}

	private class IfStateTransition extends SameStateSymbolTransition<IExprNode<TypedValue>> {
		public IfStateTransition(IParserState<IExprNode<TypedValue>> parentState) {
			super(parentState);
		}

		@Override
		public IExprNode<TypedValue> createRootNode(final List<IExprNode<TypedValue>> children) {
			return new IfNode(children);
		}
	}

	public ISymbolCallStateTransition<IExprNode<TypedValue>> createStateTransition(IParserState<IExprNode<TypedValue>> parentState) {
		return new IfStateTransition(parentState);
	}

	private class IfSymbol extends FixedCallable<TypedValue> {

		public IfSymbol() {
			super(3, 1);
		}

		@Override
		public void call(Frame<TypedValue> frame) {
			final TypedValue ifFalse = frame.stack().pop();
			ifFalse.checkType(Code.class, "third (false branch) 'if' parameter");

			final TypedValue ifTrue = frame.stack().pop();
			ifTrue.checkType(Code.class, "second (true branch) 'if' parameter");

			final TypedValue condition = frame.stack().pop();
			(MetaObjectUtils.boolValue(frame, condition)? ifTrue : ifFalse).as(Code.class).execute(frame);
		}
	}

	public void registerSymbol(Environment<TypedValue> env) {
		env.setGlobalSymbol(TypedCalcConstants.SYMBOL_IF, new IfSymbol());
	}
}
