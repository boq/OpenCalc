package info.openmods.calc.parsing.ast;

import com.google.common.collect.PeekingIterator;
import info.openmods.calc.parsing.token.Token;

public interface IAstParser<E> {
	public E parse(IParserState<E> state, PeekingIterator<Token> input);
}
