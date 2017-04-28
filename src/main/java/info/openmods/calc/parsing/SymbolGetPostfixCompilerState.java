package info.openmods.calc.parsing;

import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.SymbolGet;
import info.openmods.calc.parsing.postfix.SingleTokenPostfixParserState;
import info.openmods.calc.parsing.token.Token;
import info.openmods.calc.parsing.token.TokenType;

public class SymbolGetPostfixCompilerState<E> extends SingleTokenPostfixParserState<IExecutable<E>> {
	@Override
	protected IExecutable<E> parseToken(Token token) {
		if (token.type == TokenType.SYMBOL)
			return new SymbolGet<E>(token.value);

		return rejectToken();
	}
}