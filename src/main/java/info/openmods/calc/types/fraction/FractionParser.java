package info.openmods.calc.types.fraction;

import info.openmods.calc.parsing.IValueParser;
import info.openmods.calc.parsing.PositionalNotationParser;
import info.openmods.calc.parsing.token.Token;
import org.apache.commons.lang3.math.Fraction;
import org.apache.commons.lang3.tuple.Pair;

public class FractionParser implements IValueParser<Fraction> {

	private static final PositionalNotationParser<Fraction, Fraction> PARSER = new PositionalNotationParser<Fraction, Fraction>() {
		@Override
		public Accumulator<Fraction> createIntegerAccumulator(final int radix) {
			final Fraction fractionalRadix = Fraction.getFraction(radix, 1);
			return new Accumulator<Fraction>() {
				private Fraction value = Fraction.ZERO;

				@Override
				public void add(int digit) {
					value = value.multiplyBy(fractionalRadix).add(Fraction.getFraction(digit, 1));
				}

				@Override
				public Fraction get() {
					return value;
				}
			};
		}

		@Override
		protected Accumulator<Fraction> createFractionalAccumulator(final int radix) {
			return new Accumulator<Fraction>() {
				private Fraction value = Fraction.ZERO;
				private int weight = radix;

				@Override
				public void add(int digit) {
					value = value.add(Fraction.getFraction(digit, weight));
					weight *= radix;
				}

				@Override
				public Fraction get() {
					return value.reduce();
				}
			};
		}
	};

	@Override
	public Fraction parseToken(Token token) {
		final Pair<Fraction, Fraction> result = PARSER.parseToken(token);
		final Fraction left = result.getLeft();
		final Fraction right = result.getRight();

		return right != null? left.add(right) : left;
	}

}
