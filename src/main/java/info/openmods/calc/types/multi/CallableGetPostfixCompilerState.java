package info.openmods.calc.types.multi;

import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.Operator;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.SymbolGetPostfixCompilerState;
import info.openmods.calc.parsing.ast.IOperatorDictionary;
import info.openmods.calc.parsing.ast.OperatorArity;
import info.openmods.calc.parsing.token.Token;
import info.openmods.calc.parsing.token.TokenType;
import info.openmods.calc.symbol.CallableOperatorWrapper;
import info.openmods.calc.symbol.ICallable;

public class CallableGetPostfixCompilerState extends SymbolGetPostfixCompilerState<TypedValue> {

	private final IOperatorDictionary<Operator<TypedValue>> operators;
	private final TypeDomain domain;

	public CallableGetPostfixCompilerState(IOperatorDictionary<Operator<TypedValue>> operators, TypeDomain domain) {
		this.operators = operators;
		this.domain = domain;
	}

	@Override
	protected IExecutable<TypedValue> parseToken(Token token) {
		if (token.type == TokenType.OPERATOR) {
			Operator<TypedValue> op = operators.getOperator(token.value, OperatorArity.BINARY);
			if (op == null) op = operators.getOperator(token.value, OperatorArity.UNARY);
			if (op == null) return rejectToken();
			return createGetter(new CallableOperatorWrapper(op));
		}

		return super.parseToken(token);
	}

	private IExecutable<TypedValue> createGetter(ICallable<TypedValue> wrapper) {
		return Value.create(CallableValue.wrap(domain, wrapper));
	}

}
