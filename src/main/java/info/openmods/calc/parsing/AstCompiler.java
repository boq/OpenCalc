package info.openmods.calc.parsing;

import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import info.openmods.calc.executable.ExecutableList;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.parsing.ast.IAstParser;
import info.openmods.calc.parsing.ast.IParserState;
import info.openmods.calc.parsing.node.IExprNode;
import info.openmods.calc.parsing.token.Token;
import java.util.List;

public class AstCompiler<E> implements ITokenStreamCompiler<E> {

	private final IParserState<IExprNode<E>> initialCompilerState;

	public AstCompiler(IParserState<IExprNode<E>> initialCompilerState) {
		this.initialCompilerState = initialCompilerState;
	}

	@Override
	public IExecutable<E> compile(PeekingIterator<Token> input) {
		final IAstParser<IExprNode<E>> parser = initialCompilerState.getParser();
		final IExprNode<E> rootNode = parser.parse(initialCompilerState, input);

		final List<IExecutable<E>> output = Lists.newArrayList();
		rootNode.flatten(output);
		return ExecutableList.wrap(output);
	}

}
