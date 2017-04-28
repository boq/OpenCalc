package info.openmods.calc.types.multi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.SymbolCall;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.node.IExprNode;
import java.util.List;

public class SliceCallNode implements IExprNode<TypedValue> {

	public final IExprNode<TypedValue> target;
	public final IExprNode<TypedValue> args;

	public SliceCallNode(IExprNode<TypedValue> target, IExprNode<TypedValue> args) {
		this.target = target;
		this.args = args;
	}

	@Override
	public void flatten(List<IExecutable<TypedValue>> output) {
		target.flatten(output);
		flattenArgsAndSymbol(output);
	}

	protected void flattenArgsAndSymbol(final List<IExecutable<TypedValue>> output) {
		int argCount = 0;
		for (IExprNode<TypedValue> arg : args.getChildren()) {
			arg.flatten(output);
			argCount++;
		}

		output.add(new SymbolCall<TypedValue>(TypedCalcConstants.SYMBOL_SLICE, argCount + 1, 1));
	}

	@Override
	public Iterable<IExprNode<TypedValue>> getChildren() {
		final ImmutableList.Builder<IExprNode<TypedValue>> builder = ImmutableList.builder();
		builder.add(target);
		builder.addAll(args.getChildren());
		return builder.build();
	}

	public static class NullAware extends SliceCallNode {

		private final TypeDomain domain;

		public NullAware(IExprNode<TypedValue> target, IExprNode<TypedValue> args, TypeDomain domain) {
			super(target, args);
			this.domain = domain;
		}

		@Override
		public void flatten(List<IExecutable<TypedValue>> output) {
			target.flatten(output);

			final List<IExecutable<TypedValue>> nonNullOp = Lists.newArrayList();
			flattenArgsAndSymbol(nonNullOp);

			output.add(Value.create(Code.wrap(domain, nonNullOp)));

			output.add(new SymbolCall<TypedValue>(TypedCalcConstants.SYMBOL_NULL_EXECUTE, 2, 1));
		}
	}
}
