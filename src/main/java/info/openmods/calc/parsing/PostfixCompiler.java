package info.openmods.calc.parsing;

import com.google.common.collect.PeekingIterator;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.parsing.postfix.PostfixParser;
import info.openmods.calc.parsing.token.Token;

public class PostfixCompiler<E> implements ITokenStreamCompiler<E> {

	private final PostfixParser<IExecutable<E>> parser;

	public PostfixCompiler(PostfixParser<IExecutable<E>> parser) {
		this.parser = parser;
	}

	@Override
	public IExecutable<E> compile(PeekingIterator<Token> input) {
		return parser.parse(input);
	}

}
