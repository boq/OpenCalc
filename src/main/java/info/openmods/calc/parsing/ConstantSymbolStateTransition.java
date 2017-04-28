package info.openmods.calc.parsing;

import com.google.common.collect.Lists;
import info.openmods.calc.Environment;
import info.openmods.calc.Frame;
import info.openmods.calc.executable.ExecutableList;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.ast.IParserState;
import info.openmods.calc.parsing.ast.SameStateSymbolTransition;
import info.openmods.calc.parsing.node.IExprNode;
import info.openmods.calc.parsing.node.SymbolCallNode;
import java.util.List;

public class ConstantSymbolStateTransition<E> extends SameStateSymbolTransition<IExprNode<E>> {

	private final String selfSymbol;
	private final Environment<E> env;

	public ConstantSymbolStateTransition(IParserState<IExprNode<E>> parentState, Environment<E> env, String selfSymbol) {
		super(parentState);
		this.env = env;
		this.selfSymbol = selfSymbol;
	}

	private class ConstantsNode extends SymbolCallNode<E> {
		public ConstantsNode(List<IExprNode<E>> constants) {
			super(selfSymbol, constants);
		}

		@Override
		public void flatten(List<IExecutable<E>> output) {
			final List<IExecutable<E>> ops = Lists.newArrayList();
			for (IExprNode<E> child : getChildren())
				child.flatten(ops);

			final Frame<E> resultFrame = env.executeIsolated(ExecutableList.wrap(ops));

			for (E constant : resultFrame.stack())
				output.add(Value.create(constant));
		}

	}

	@Override
	public IExprNode<E> createRootNode(List<IExprNode<E>> children) {
		return new ConstantsNode(children);
	}

}
