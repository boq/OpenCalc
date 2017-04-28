package info.openmods.calc.parsing;

import com.google.common.collect.PeekingIterator;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.parsing.token.Token;

public interface ITokenStreamCompiler<E> {
	public IExecutable<E> compile(PeekingIterator<Token> input);
}
