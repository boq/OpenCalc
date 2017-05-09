package info.openmods.calc.types.fp;

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
import info.openmods.calc.symbol.GenericFunctions.DirectAccumulatorFunction;
import info.openmods.calc.symbol.NullaryFunction;
import info.openmods.calc.symbol.UnaryFunction;
import java.util.Random;

public class DoubleCalculatorFactory<M> extends SimpleCalculatorFactory<Double, M> {
	public static final double NULL_VALUE = 0.0;

	@Override
	protected IValueParser<Double> getValueParser() {
		return new DoubleParser();
	}

	@Override
	protected Double getNullValue() {
		return NULL_VALUE;
	}

	@Override
	protected IValuePrinter<Double> createValuePrinter() {
		return new DoublePrinter();
	}

	@Override
	protected void configureEnvironment(Environment<Double> env) {
		env.setGlobalSymbol("PI", Math.PI);
		env.setGlobalSymbol("E", Math.E);
		env.setGlobalSymbol("INF", Double.POSITIVE_INFINITY);
		env.setGlobalSymbol("MAX", Double.MIN_VALUE);

		env.setGlobalSymbol("abs", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.abs(value);
			}
		});

		env.setGlobalSymbol("sgn", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.signum(value);
			}
		});

		env.setGlobalSymbol("sqrt", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.sqrt(value);
			}
		});

		env.setGlobalSymbol("ceil", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.ceil(value);
			}
		});

		env.setGlobalSymbol("floor", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.floor(value);
			}
		});

		env.setGlobalSymbol("cos", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.cos(value);
			}
		});

		env.setGlobalSymbol("cosh", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.cosh(value);
			}
		});

		env.setGlobalSymbol("sin", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.sin(value);
			}
		});

		env.setGlobalSymbol("sinh", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.sinh(value);
			}
		});

		env.setGlobalSymbol("tan", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.tan(value);
			}
		});

		env.setGlobalSymbol("tanh", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.tanh(value);
			}
		});

		env.setGlobalSymbol("acos", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.acos(value);
			}
		});

		env.setGlobalSymbol("acosh", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.log(value + Math.sqrt(value * value - 1));
			}
		});

		env.setGlobalSymbol("asin", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.asin(value);
			}
		});

		env.setGlobalSymbol("asinh", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return value.isInfinite()? value : Math.log(value + Math.sqrt(value * value + 1));
			}
		});

		env.setGlobalSymbol("atan", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.atan(value);
			}
		});

		env.setGlobalSymbol("atanh", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.log((1 + value) / (1 - value)) / 2;
			}
		});

		env.setGlobalSymbol("atan2", new BinaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double left, Double right) {
				return Math.atan2(left, right);
			}

		});

		env.setGlobalSymbol("log10", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.log10(value);
			}
		});

		env.setGlobalSymbol("ln", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.log(value);
			}
		});

		env.setGlobalSymbol("log", new BinaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double left, Double right) {
				return Math.log(left) / Math.log(right);
			}
		});

		env.setGlobalSymbol("exp", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.exp(value);
			}
		});

		env.setGlobalSymbol("min", new DirectAccumulatorFunction<Double>(NULL_VALUE) {
			@Override
			protected Double accumulate(Double result, Double value) {
				return Math.min(result, value);
			}
		});

		env.setGlobalSymbol("max", new DirectAccumulatorFunction<Double>(NULL_VALUE) {
			@Override
			protected Double accumulate(Double result, Double value) {
				return Math.max(result, value);
			}
		});

		env.setGlobalSymbol("sum", new DirectAccumulatorFunction<Double>(NULL_VALUE) {
			@Override
			protected Double accumulate(Double result, Double value) {
				return result + value;
			}
		});

		env.setGlobalSymbol("avg", new DirectAccumulatorFunction<Double>(NULL_VALUE) {
			@Override
			protected Double accumulate(Double result, Double value) {
				return result + value;
			}

			@Override
			protected Double process(Double result, int argCount) {
				return result / argCount;
			}
		});

		env.setGlobalSymbol("rad", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.toRadians(value);
			}
		});

		env.setGlobalSymbol("deg", new UnaryFunction.Direct<Double>() {
			@Override
			protected Double call(Double value) {
				return Math.toDegrees(value);
			}
		});

		final Random random = new Random();

		env.setGlobalSymbol("rand", new NullaryFunction.Direct<Double>() {
			@Override
			protected Double call() {
				return random.nextDouble();
			}
		});

		env.setGlobalSymbol("gauss", new NullaryFunction.Direct<Double>() {
			@Override
			protected Double call() {
				return random.nextGaussian();
			}
		});
	}

	private static final int PRIORITY_POWER = 4;
	private static final int PRIORITY_MULTIPLY = 3;
	private static final int PRIORITY_ADD = 2;
	private static final int PRIORITY_ASSIGN = 1;

	@Override
	protected void configureOperators(OperatorDictionary<Operator<Double>> operators) {
		operators.registerOperator(new UnaryOperator.Direct<Double>("neg") {
			@Override
			public Double execute(Double value) {
				return -value;
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<Double>("+", PRIORITY_ADD) {
			@Override
			public Double execute(Double left, Double right) {
				return left + right;
			}
		});

		operators.registerOperator(new UnaryOperator.Direct<Double>("+") {
			@Override
			public Double execute(Double value) {
				return +value;
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<Double>("-", PRIORITY_ADD) {
			@Override
			public Double execute(Double left, Double right) {
				return left - right;
			}
		});

		operators.registerOperator(new UnaryOperator.Direct<Double>("-") {
			@Override
			public Double execute(Double value) {
				return -value;
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<Double>("*", PRIORITY_MULTIPLY) {
			@Override
			public Double execute(Double left, Double right) {
				return left * right;
			}
		}).setDefault();

		operators.registerOperator(new BinaryOperator.Direct<Double>("/", PRIORITY_MULTIPLY) {
			@Override
			public Double execute(Double left, Double right) {
				return left / right;
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<Double>("%", PRIORITY_MULTIPLY) {
			@Override
			public Double execute(Double left, Double right) {
				return left % right;
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<Double>("^", PRIORITY_POWER) {
			@Override
			public Double execute(Double left, Double right) {
				return Math.pow(left, right);
			}
		});

		operators.registerOperator(new BinaryOperator.Direct<Double>("**", PRIORITY_POWER) {
			@Override
			public Double execute(Double left, Double right) {
				return Math.pow(left, right);
			}
		});
	}

	public static Calculator<Double, ExprType> createSimple() {
		return new DoubleCalculatorFactory<ExprType>().create(new BasicCompilerMapFactory<Double>());
	}

	public static Calculator<Double, ExprType> createDefault() {
		final CommonSimpleSymbolFactory<Double> letFactory = new CommonSimpleSymbolFactory<Double>(PRIORITY_ASSIGN, ":", "=");

		return new DoubleCalculatorFactory<ExprType>() {
			@Override
			protected void configureOperators(OperatorDictionary<Operator<Double>> operators) {
				super.configureOperators(operators);
				letFactory.registerSeparators(operators);
			}
		}.create(letFactory.createCompilerFactory());
	}
}
