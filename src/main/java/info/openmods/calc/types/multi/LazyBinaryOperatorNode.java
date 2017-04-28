package info.openmods.calc.types.multi;

import com.google.common.collect.Lists;
import info.openmods.calc.executable.BinaryOperator;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.SymbolCall;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.node.BinaryOpNode;
import info.openmods.calc.parsing.node.IExprNode;
import info.openmods.calc.parsing.node.MappedExprNodeFactory.IBinaryExprNodeFactory;
import java.util.List;

public class LazyBinaryOperatorNode extends BinaryOpNode<TypedValue> {

	private final TypeDomain domain;
	private final String symbolName;

	public LazyBinaryOperatorNode(BinaryOperator<TypedValue> operator, IExprNode<TypedValue> left, IExprNode<TypedValue> right, TypeDomain domain, String symbolName) {
		super(operator, left, right);
		this.domain = domain;
		this.symbolName = symbolName;
	}

	@Override
	public void flatten(List<IExecutable<TypedValue>> output) {
		final List<IExprNode<TypedValue>> args = Lists.newArrayList();
		collectArgs(args);

		for (IExprNode<TypedValue> arg : args)
			output.add(Value.create(Code.flattenAndWrap(domain, arg)));

		output.add(new SymbolCall<TypedValue>(symbolName, args.size(), 1));
	}

	private void collectArgs(List<IExprNode<TypedValue>> args) {
		addNode(args, left);
		addNode(args, right);
	}

	private void addNode(List<IExprNode<TypedValue>> output, IExprNode<TypedValue> node) {
		if (node instanceof LazyBinaryOperatorNode) {
			final LazyBinaryOperatorNode opNode = (LazyBinaryOperatorNode)node;
			if (opNode.operator == this.operator) {
				opNode.collectArgs(output);
				return;
			}
		}

		output.add(node);
	}

	public static IBinaryExprNodeFactory<TypedValue> createFactory(final BinaryOperator<TypedValue> op, final TypeDomain domain, final String implSymbol) {
		return new IBinaryExprNodeFactory<TypedValue>() {
			@Override
			public IExprNode<TypedValue> create(IExprNode<TypedValue> leftChild, IExprNode<TypedValue> rightChild) {
				return new LazyBinaryOperatorNode(op, leftChild, rightChild, domain, implSymbol);
			}
		};
	}
}
