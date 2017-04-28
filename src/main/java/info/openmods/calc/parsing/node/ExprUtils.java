package info.openmods.calc.parsing.node;

import com.google.common.collect.Lists;
import info.openmods.calc.executable.ExecutableList;
import info.openmods.calc.executable.IExecutable;
import java.util.List;

public class ExprUtils {

	public static <E> IExecutable<E> flattenNode(IExprNode<E> node) {
		final List<IExecutable<E>> commands = Lists.newArrayList();
		node.flatten(commands);
		return ExecutableList.wrap(commands);
	}

}
