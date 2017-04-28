package info.openmods.calc.types.bigint;

import info.openmods.calc.IValuePrinter;
import info.openmods.calc.PositionalNotationPrinter;
import info.openmods.calc.PrinterUtils;
import info.openmods.calc.utils.config.Configurable;
import java.math.BigInteger;
import org.apache.commons.lang3.tuple.Pair;

public class BigIntPrinter implements IValuePrinter<BigInteger> {

	public static class Helper extends PositionalNotationPrinter<BigInteger> {
		public Helper(int maxDigits) {
			super(maxDigits);
		}

		@Override
		protected Pair<BigInteger, BigInteger> splitNumber(BigInteger value) {
			return Pair.of(value, null);
		}

		@Override
		protected IDigitProvider createIntegerDigitProvider(final BigInteger value, int radix) {
			final BigInteger bigRadix = BigInteger.valueOf(radix);
			return new IDigitProvider() {

				private BigInteger remainder = value;

				@Override
				public int getNextDigit() {
					final BigInteger[] divideAndRemainder = remainder.divideAndRemainder(bigRadix);
					remainder = divideAndRemainder[0];
					return divideAndRemainder[1].intValue();
				}

				@Override
				public boolean hasNextDigit() {
					return !checkIsZero(remainder);
				}

			};
		}

		@Override
		protected IDigitProvider createFractionalDigitProvider(BigInteger value, int radix) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected boolean isNegative(BigInteger value) {
			return value.signum() < 0;
		}

		@Override
		protected BigInteger negate(BigInteger value) {
			return value.negate();
		}

		@Override
		protected boolean isZero(BigInteger value) {
			return checkIsZero(value);
		}

		private static boolean checkIsZero(BigInteger value) {
			return value.equals(BigInteger.ZERO);
		}
	}

	private final PositionalNotationPrinter<BigInteger> printer = new Helper(0);

	@Configurable
	public int base = 10;

	@Configurable
	public boolean uniformBaseNotation = false;

	@Override
	public String str(BigInteger value) {
		if (base < Character.MIN_RADIX) return "invalid radix";
		return PrinterUtils.decorateBase(!uniformBaseNotation, base, (base <= Character.MAX_RADIX)? value.toString(base) : printer.toString(value, base));
	}

	@Override
	public String repr(BigInteger value) {
		return str(value);
	}
}