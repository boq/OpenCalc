package info.openmods.calc.parsing.node;

import com.google.common.collect.ImmutableList;
import info.openmods.calc.executable.BinaryOperator;
import info.openmods.calc.executable.IExecutable;
import java.util.List;

public class BinaryOpNode<E> implements IExprNode<E> {

	public final BinaryOperator<E> operator;

	public final IExprNode<E> left;

	public final IExprNode<E> right;

	public BinaryOpNode(BinaryOperator<E> operator, IExprNode<E> left, IExprNode<E> right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	@Override
	public void flatten(List<IExecutable<E>> output) {
		left.flatten(output);
		right.flatten(output);
		output.add(operator);
	}

	@Override
	public String toString() {
		return "<op: '" + operator.id + "', l: " + left + ", r: " + right + ">";
	}

	@Override
	public Iterable<IExprNode<E>> getChildren() {
		return ImmutableList.of(left, right);
	}
}
