package info.openmods.calc.types.bigint;

import info.openmods.calc.Calculator;
import info.openmods.calc.Environment;
import info.openmods.calc.ExprType;
import info.openmods.calc.IValuePrinter;
import info.openmods.calc.SimpleCalculatorFactory;
import info.openmods.calc.executable.BinaryOperator;
import info.openmods.calc.executable.Operator;
import info.openmods.calc.executable.OperatorDictionary;
import info.openmods.calc.executable.UnaryOperator;
import info.openmods.calc.parsing.BasicCompilerMapFactory;
import info.openmods.calc.parsing.CommonSimpleSymbolFactory;
import info.openmods.calc.parsing.IValueParser;
import info.openmods.calc.symbol.BinaryFunction;
import info.openmods.calc.symbol.GenericFunctions.AccumulatorFunction;
import info.openmods.calc.symbol.NullaryFunction;
import info.openmods.calc.symbol.TernaryFunction;
import info.openmods.calc.symbol.UnaryFunction;
import java.math.BigInteger;
import java.util.Random;

public class BigIntCalculatorFactory<M> extends SimpleCalculatorFactory<BigInteger, M> {

	public static final BigInteger NULL_VALUE = BigInteger.ZERO;

	@Override
	protected IValueParser<BigInteger> getValueParser() {
		return new BigIntParser();
	}

	@Override
	protected BigInteger getNullValue() {
		return NULL_VALUE;
	}

	@Override
	protected IValuePrinter<BigInteger> createValuePrinter() {
		return new BigIntPrinter();
	}

	@Override
	protected void configureEnvironment(Environment<BigInteger> env) {
		env.setGlobalSymbol("abs", new UnaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger value) {
				return value.abs();
			}
		});

		env.setGlobalSymbol("sgn", new UnaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger value) {
				return BigInteger.valueOf(value.signum());
			}
		});

		env.setGlobalSymbol("min", new AccumulatorFunction<BigInteger>(NULL_VALUE) {
			@Override
			protected BigInteger accumulate(BigInteger result, BigInteger value) {
				return result.min(value);
			}
		});

		env.setGlobalSymbol("max", new AccumulatorFunction<BigInteger>(NULL_VALUE) {
			@Override
			protected BigInteger accumulate(BigInteger result, BigInteger value) {
				return result.max(value);
			}
		});

		env.setGlobalSymbol("sum", new AccumulatorFunction<BigInteger>(NULL_VALUE) {
			@Override
			protected BigInteger accumulate(BigInteger result, BigInteger value) {
				return result.add(value);
			}
		});

		env.setGlobalSymbol("avg", new AccumulatorFunction<BigInteger>(NULL_VALUE) {
			@Override
			protected BigInteger accumulate(BigInteger result, BigInteger value) {
				return result.add(value);
			}

			@Override
			protected BigInteger process(BigInteger result, int argCount) {
				return result.divide(BigInteger.valueOf(argCount));
			}

		});

		env.setGlobalSymbol("gcd", new BinaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger left, BigInteger right) {
				return left.gcd(right);
			}
		});

		env.setGlobalSymbol("gcd", new BinaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger left, BigInteger right) {
				return left.gcd(right);
			}
		});

		env.setGlobalSymbol("modpow", new TernaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger first, BigInteger second, BigInteger third) {
				return first.modPow(second, third);
			}
		});

		env.setGlobalSymbol("get", new BinaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger first, BigInteger second) {
				return first.testBit(second.intValue())? BigInteger.ONE : BigInteger.ZERO;
			}
		});

		env.setGlobalSymbol("set", new BinaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger first, BigInteger second) {
				return first.setBit(second.intValue());
			}
		});

		env.setGlobalSymbol("clear", new BinaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger first, BigInteger second) {
				return first.clearBit(second.intValue());
			}
		});

		env.setGlobalSymbol("flip", new BinaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call(BigInteger first, BigInteger second) {
				return first.flipBit(second.intValue());
			}
		});

		final Random random = new Random();

		env.setGlobalSymbol("rand", new NullaryFunction.Direct<BigInteger>() {
			@Override
			protected BigInteger call() {
				return BigInteger.valueOf(random.nextLong());
			}
		});
	}

	private static final int PRIORITY_EXP = 6;
	private static final int PRIORITY_MULTIPLY = 5;
	private static final int PRIORITY_ADD = 4;
	private static final int PRIORITY_BITSHIFT = 3;
	private static final int PRIORITY_BITWISE = 2;
	private static final int PRIORITY_ASSIGN = 1;

	@Override
	protected void configureOperators(OperatorDictionary<Operator<BigInteger>> operators) {
		operators.registerOperator(new UnaryOperator.Direct<BigInteger>("~") {
			@Override
			public BigInteger execute(BigInteger value) {
				return value.not();
			}
		});

		operators.registerOperator(new UnaryOperator.Direct<BigInteger>("neg") {
			@Override
			public BigInteger execute(BigInteger value) {
				return value.negate();
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("^", PRIORITY_BITWISE) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.xor(right);
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("|", PRIORITY_BITWISE) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.or(right);
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("&", PRIORITY_BITWISE) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.and(right);
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("+", PRIORITY_ADD) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.add(right);
			}
		});

		operators.registerOperator(new UnaryOperator.Direct<BigInteger>("+") {
			@Override
			public BigInteger execute(BigInteger value) {
				return value;
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("-", PRIORITY_ADD) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.subtract(right);
			}
		});

		operators.registerOperator(new UnaryOperator.Direct<BigInteger>("-") {
			@Override
			public BigInteger execute(BigInteger value) {
				return value.negate();
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("*", PRIORITY_MULTIPLY) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.multiply(right);
			}
		}).setDefault();

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("/", PRIORITY_MULTIPLY) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.divide(right);
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("%", PRIORITY_MULTIPLY) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.mod(right);
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("**", PRIORITY_EXP) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.pow(right.intValue());
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>("<<", PRIORITY_BITSHIFT) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.shiftLeft(right.intValue());
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<BigInteger>(">>", PRIORITY_BITSHIFT) {
			@Override
			public BigInteger execute(BigInteger left, BigInteger right) {
				return left.shiftRight(right.intValue());
			}
		});
	}

	public static Calculator<BigInteger, ExprType> createSimple() {
		return new BigIntCalculatorFactory<ExprType>().create(new BasicCompilerMapFactory<BigInteger>());
	}

	public static Calculator<BigInteger, ExprType> createDefault() {
		final CommonSimpleSymbolFactory<BigInteger> letFactory = new CommonSimpleSymbolFactory<BigInteger>(PRIORITY_ASSIGN, ":", "=");

		return new BigIntCalculatorFactory<ExprType>() {
			@Override
			protected void configureOperators(OperatorDictionary<Operator<BigInteger>> operators) {
				super.configureOperators(operators);
				letFactory.registerSeparators(operators);
			}
		}.create(letFactory.createCompilerFactory());
	}

}
