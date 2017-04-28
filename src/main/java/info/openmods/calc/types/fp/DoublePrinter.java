package info.openmods.calc.types.fp;

import info.openmods.calc.IValuePrinter;
import info.openmods.calc.PositionalNotationPrinter;
import info.openmods.calc.PrinterUtils;
import info.openmods.calc.utils.config.Configurable;
import org.apache.commons.lang3.tuple.Pair;

public class DoublePrinter implements IValuePrinter<Double> {

	public static class Helper extends PositionalNotationPrinter<Double> {
		public Helper(int maxDigits) {
			super(maxDigits);
		}

		@Override
		protected Pair<Double, Double> splitNumber(Double value) {
			final double integer = value.intValue();
			final double fractional = value - integer;
			return Pair.of(integer, fractional);
		}

		@Override
		protected IDigitProvider createIntegerDigitProvider(final Double value, final int radix) {
			return new IDigitProvider() {
				// should already be int
				private int remainder = value.intValue();

				@Override
				public int getNextDigit() {
					final int digit = remainder % radix;
					remainder /= radix;
					return digit;
				}

				@Override
				public boolean hasNextDigit() {
					return remainder > 0;
				}
			};
		}

		@Override
		protected IDigitProvider createFractionalDigitProvider(final Double value, int radix) {
			final double doubleRadix = radix;
			return new IDigitProvider() {
				private double remainder = value;

				@Override
				public int getNextDigit() {
					// very naive (read: stupid) algorithm
					remainder *= doubleRadix;
					int result = (int)remainder;
					remainder %= 1;
					return result;
				}

				@Override
				public boolean hasNextDigit() {
					return remainder > 0;
				}
			};
		}

		@Override
		protected boolean isNegative(Double value) {
			return value < 0;
		}

		@Override
		protected Double negate(Double value) {
			return -value;
		}

		@Override
		protected boolean isZero(Double value) {
			return value == 0;
		}
	}

	private final PositionalNotationPrinter<Double> printer = new Helper(8);

	@Configurable
	public int base = 10;

	@Configurable
	public boolean allowStandardPrinter = false;

	@Configurable
	public boolean uniformBaseNotation = false;

	@Override
	public String str(Double value) {
		if (base == 10 && !allowStandardPrinter && !uniformBaseNotation) {
			return value.toString();
		} else {
			if (value.isNaN()) return "NaN";
			if (value.isInfinite()) return value > 0? "+Inf" : "-Inf";
			final String result = printer.toString(value, base);
			return PrinterUtils.decorateBase(!uniformBaseNotation, base, result);
		}
	}

	@Override
	public String repr(Double value) {
		return str(value);
	}
}