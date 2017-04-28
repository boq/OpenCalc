package info.openmods.calc.types.multi;

import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.IValueParser;
import info.openmods.calc.parsing.postfix.SingleTokenPostfixParserState;
import info.openmods.calc.parsing.token.Token;
import info.openmods.calc.parsing.token.TokenType;

public class QuotePostfixCompilerState extends SingleTokenPostfixParserState<IExecutable<TypedValue>> {

	private final IValueParser<TypedValue> valueParser;
	private final TypeDomain domain;

	public QuotePostfixCompilerState(IValueParser<TypedValue> valueParser, TypeDomain domain) {
		this.valueParser = valueParser;
		this.domain = domain;
	}

	private static boolean canBeRaw(TokenType type) {
		return type == TokenType.MODIFIER || type == TokenType.OPERATOR || type == TokenType.SYMBOL;
	}

	@Override
	protected IExecutable<TypedValue> parseToken(Token token) {
		if (token.type.isValue()) return Value.create(valueParser.parseToken(token));
		if (canBeRaw(token.type)) return Value.create(Symbol.get(domain, token.value));

		return rejectToken();
	}

}