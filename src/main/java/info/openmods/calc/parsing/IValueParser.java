package info.openmods.calc.parsing;

import info.openmods.calc.parsing.token.Token;

public interface IValueParser<E> {
	public E parseToken(Token token);
}
