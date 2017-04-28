package info.openmods.calc.types.multi;

import com.google.common.collect.ImmutableList;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.node.IExprNode;
import java.util.List;

public class RawCodeExprNode implements IExprNode<TypedValue> {
	private final TypeDomain domain;
	private final IExprNode<TypedValue> arg;

	public RawCodeExprNode(TypeDomain domain, IExprNode<TypedValue> child) {
		this.arg = child;
		this.domain = domain;
	}

	@Override
	public void flatten(List<IExecutable<TypedValue>> output) {
		output.add(Value.create(Code.flattenAndWrap(domain, arg)));
	}

	@Override
	public Iterable<IExprNode<TypedValue>> getChildren() {
		return ImmutableList.of(arg);
	}
}