package info.openmods.calc.types.multi;

import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.SymbolCall;
import info.openmods.calc.parsing.node.IExprNode;
import info.openmods.calc.parsing.node.SquareBracketContainerNode;
import java.util.List;

public class ListBracketNode extends SquareBracketContainerNode<TypedValue> {

	public ListBracketNode(List<IExprNode<TypedValue>> args) {
		super(args);
	}

	@Override
	public void flatten(List<IExecutable<TypedValue>> output) {
		for (IExprNode<TypedValue> node : args)
			node.flatten(output);

		output.add(new SymbolCall<TypedValue>(TypedCalcConstants.SYMBOL_LIST, args.size(), 1));
	}
}
