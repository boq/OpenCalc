package info.openmods.calc.parsing.node;

import com.google.common.collect.ImmutableList;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.Value;
import java.util.List;

public class ValueNode<E> implements IExprNode<E> {

	public final E value;

	public ValueNode(E value) {
		this.value = value;
	}

	@Override
	public void flatten(List<IExecutable<E>> output) {
		output.add(Value.create(value));
	}

	@Override
	public String toString() {
		return "<v: " + value + ">";
	}

	@Override
	public Iterable<IExprNode<E>> getChildren() {
		return ImmutableList.of();
	}
}
