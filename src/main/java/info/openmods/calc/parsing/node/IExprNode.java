package info.openmods.calc.parsing.node;

import info.openmods.calc.executable.IExecutable;
import java.util.List;

public interface IExprNode<E> {

	public void flatten(List<IExecutable<E>> output);

	public Iterable<IExprNode<E>> getChildren();
}
