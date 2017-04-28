package info.openmods.calc.types.multi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import info.openmods.calc.Frame;
import info.openmods.calc.FrameFactory;
import info.openmods.calc.symbol.ICallable;
import info.openmods.calc.symbol.SymbolMap;
import info.openmods.calc.utils.OptionalInt;
import info.openmods.calc.utils.Stack;
import java.util.Iterator;
import java.util.List;

public class ClosureVar implements ICallable<TypedValue> {

	private final TypedValue nullValue;

	private final SymbolMap<TypedValue> scopeSymbols;

	private final Code code;

	private final List<IBindPattern> args;

	private final String varArgName;

	public ClosureVar(TypedValue nullValue, SymbolMap<TypedValue> scopeSymbols, Code code, List<IBindPattern> args, String varArg) {
		this.nullValue = nullValue;
		this.code = code;
		this.scopeSymbols = scopeSymbols;
		this.args = ImmutableList.copyOf(args);
		this.varArgName = varArg;
	}

	@Override
	public void call(Frame<TypedValue> callsite, OptionalInt argumentsCount, OptionalInt returnsCount) {
		final int allArgs;
		final int mandatoryArgs = args.size();

		if (argumentsCount.isPresent()) {
			allArgs = argumentsCount.get();
			Preconditions.checkState(allArgs >= mandatoryArgs, "Invalid numer or arguments, expected more than %s, got %s", allArgs, mandatoryArgs);
		} else {
			allArgs = mandatoryArgs;
		}

		final int extraArgs = allArgs - mandatoryArgs;

		final Frame<TypedValue> executionFrame = FrameFactory.newClosureFrame(scopeSymbols, callsite, allArgs);
		final Stack<TypedValue> executionStack = executionFrame.stack();

		final SymbolMap<TypedValue> executionSymbols = executionFrame.symbols();

		final TypeDomain domain = nullValue.domain;
		TypedValue varArgValue = nullValue;

		for (int i = 0; i < extraArgs; i++) {
			final TypedValue arg = executionStack.pop();
			varArgValue = Cons.create(domain, arg, varArgValue);
		}

		final Iterator<TypedValue> argValues = executionStack.iterator();
		for (IBindPattern argPattern : args) {
			final TypedValue argValue = argValues.next();
			TypedCalcUtils.matchPattern(argPattern, executionFrame, executionSymbols, argValue);
		}

		executionStack.clear();

		executionSymbols.put(varArgName, varArgValue);

		code.execute(executionFrame);

		TypedCalcUtils.expectExactReturnCount(returnsCount, executionStack.size());
	}

}
