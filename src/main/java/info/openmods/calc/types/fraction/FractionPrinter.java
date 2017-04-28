package info.openmods.calc.types.fraction;

import info.openmods.calc.IValuePrinter;
import info.openmods.calc.utils.config.Configurable;
import org.apache.commons.lang3.math.Fraction;

public class FractionPrinter implements IValuePrinter<Fraction> {

	@Configurable
	public boolean properFractions;

	@Configurable
	public boolean expand;

	@Override
	public String str(Fraction value) {
		if (expand) return Double.toString(value.doubleValue());
		return properFractions? value.toProperString() : value.toString();
	}

	@Override
	public String repr(Fraction value) {
		return str(value);
	}

}
