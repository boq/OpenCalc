package info.openmods.calc.types.bigint;

import info.openmods.calc.parsing.IValueParser;
import info.openmods.calc.parsing.PositionalNotationParser;
import info.openmods.calc.parsing.token.Token;
import java.math.BigInteger;
import org.apache.commons.lang3.tuple.Pair;

public class BigIntParser implements IValueParser<BigInteger> {

	private static final PositionalNotationParser<BigInteger, Void> PARSER = new PositionalNotationParser<BigInteger, Void>() {
		@Override
		public Accumulator<BigInteger> createIntegerAccumulator(int radix) {
			final BigInteger bigRadix = BigInteger.valueOf(radix);
			return new Accumulator<BigInteger>() {
				private BigInteger value = BigInteger.ZERO;

				@Override
				public void add(int digit) {
					value = value.multiply(bigRadix).add(BigInteger.valueOf(digit));
				}

				@Override
				public BigInteger get() {
					return value;
				}
			};
		}

		@Override
		public Accumulator<Void> createFractionalAccumulator(int radix) {
			throw new IllegalArgumentException("Fractional part not allowed");
		}
	};

	@Override
	public BigInteger parseToken(Token token) {
		final Pair<BigInteger, Void> result = PARSER.parseToken(token);
		return result.getLeft();
	}

}
