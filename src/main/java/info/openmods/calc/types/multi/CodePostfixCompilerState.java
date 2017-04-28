package info.openmods.calc.types.multi;

import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.postfix.BracketPostfixParserStateBase;
import info.openmods.calc.parsing.postfix.IExecutableListBuilder;

public class CodePostfixCompilerState extends BracketPostfixParserStateBase<IExecutable<TypedValue>> {

	private final TypeDomain domain;

	public CodePostfixCompilerState(TypeDomain domain, IExecutableListBuilder<IExecutable<TypedValue>> builder, String openingBracket) {
		super(builder, openingBracket);
		this.domain = domain;
	}

	@Override
	protected IExecutable<TypedValue> processCompiledBracket(IExecutable<TypedValue> compiledExpr) {
		return Value.create(Code.wrap(domain, compiledExpr));
	}

}
