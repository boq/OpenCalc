package info.openmods.calc.types.multi;

import com.google.common.collect.ImmutableSet;
import info.openmods.calc.Frame;
import info.openmods.calc.FrameFactory;
import info.openmods.calc.IValuePrinter;
import info.openmods.calc.PositionalNotationPrinter;
import info.openmods.calc.PrinterUtils;
import info.openmods.calc.math.Complex;
import info.openmods.calc.parsing.StringEscaper;
import info.openmods.calc.types.bigint.BigIntPrinter;
import info.openmods.calc.types.fp.DoublePrinter;
import info.openmods.calc.utils.config.Configurable;
import java.math.BigInteger;
import java.util.Set;

public class TypedValuePrinter implements IValuePrinter<TypedValue> {

	private static final Set<Character> UNESCAPED_CHARS = ImmutableSet.of('\'');

	@Configurable
	public int base = 10;

	@Configurable
	public boolean uniformBaseNotation = false;

	@Configurable
	public boolean allowStandardPrinter = false;

	@Configurable
	public boolean escapeStrings = false;

	@Configurable
	public boolean numericBool = false;

	@Configurable
	public boolean printTypes = false;

	@Configurable
	public boolean printLists = true;

	@Configurable
	public boolean printNilInLists = false;

	private final PositionalNotationPrinter<Double> doublePrinter = new DoublePrinter.Helper(8);

	private final PositionalNotationPrinter<BigInteger> bigIntPrinter = new BigIntPrinter.Helper(0);

	private final TypedValue nullValue;

	public TypedValuePrinter(TypedValue nullValue) {
		this.nullValue = nullValue;
	}

	@Override
	public String str(TypedValue value) {
		final String contents;
		final MetaObject.SlotStr slotStr = value.getMetaObject().slotStr;
		if (slotStr != null) {
			final Frame<TypedValue> frame = FrameFactory.createTopFrame(); // TODO: is this safe? Probably yes
			contents = slotStr.str(value, frame);
		} else {
			contents = value.value.toString();
		}

		return printTypes? "(" + value.type + ")" + contents : contents;
	}

	@Override
	public String repr(TypedValue value) {
		final MetaObject.SlotRepr slotRepr = value.getMetaObject().slotRepr;
		if (slotRepr != null) {
			final Frame<TypedValue> frame = FrameFactory.createTopFrame(); // TODO: is this safe? Probably yes
			return slotRepr.repr(value, frame);
		} else return value.value.toString();
	}

	public String str(boolean value) {
		return numericBool? (value? "1" : "0") : (value? TypedCalcConstants.SYMBOL_FALSE : TypedCalcConstants.SYMBOL_TRUE);
	}

	public String repr(boolean value) {
		return value? TypedCalcConstants.SYMBOL_FALSE : TypedCalcConstants.SYMBOL_TRUE;
	}

	public String str(String value) {
		return escapeStrings? StringEscaper.escapeString(value, '"', UNESCAPED_CHARS) : value;
	}

	public String repr(String value) {
		return StringEscaper.escapeString(value, '"', UNESCAPED_CHARS);
	}

	public String str(BigInteger value) {
		if (base < Character.MIN_RADIX) return "invalid radix";
		return PrinterUtils.decorateBase(!uniformBaseNotation, base, (base <= Character.MAX_RADIX)? value.toString(base) : bigIntPrinter.toString(value, base));
	}

	public String repr(BigInteger value) {
		return str(value);
	}

	public String str(Double value) {
		if (base == 10 && !allowStandardPrinter && !uniformBaseNotation) {
			return value.toString();
		} else {
			if (value.isNaN()) return "NaN";
			if (value.isInfinite()) return value > 0? "+Inf" : "-Inf";
			final String result = doublePrinter.toString(value, base);
			return PrinterUtils.decorateBase(!uniformBaseNotation, base, result);
		}
	}

	public String repr(Double value) {
		return str(value);
	}

	public String str(Complex value) {
		return str(value.re) + "+" + str(value.im) + "I";
	}

	public String repr(Complex value) {
		return str(value);
	}

	public String str(Cons cons) {
		if (printLists) {
			final StringBuilder result = new StringBuilder();
			cons.visit(new Cons.BranchingVisitor() {
				@Override
				public void begin() {
					result.append("[");
				}

				@Override
				public void value(TypedValue value, boolean isLast) {
					result.append(TypedValuePrinter.this.str(value));
					if (!isLast) result.append(" ");
				}

				@Override
				public Cons.BranchingVisitor nestedValue(TypedValue value) {
					result.append("[");
					return this;
				}

				@Override
				public void end(TypedValue terminator) {
					if (terminator.value != nullValue || printNilInLists) {
						result.append(" . ");
						result.append(TypedValuePrinter.this.str(terminator));
					}
					result.append("]");
				}
			});

			return result.toString();
		} else {
			return "(" + str(cons.car) + " . " + str(cons.cdr) + ")";
		}
	}

	public String repr(Cons cons) {
		// TODO: [] notation? problem with terminators
		return repr(cons.car) + " : " + repr(cons.cdr);
	}

	public String str(Symbol s) {
		return s.value;
	}

	public String repr(Symbol s) {
		return '#' + s.value;
	}

}
