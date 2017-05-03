package info.openmods.calc.types.bool;

import info.openmods.calc.IValuePrinter;
import info.openmods.calc.utils.config.ConfigProperty;

public class BoolPrinter implements IValuePrinter<Boolean> {

	@ConfigProperty
	public boolean numeric = false;

	@Override
	public String str(Boolean value) {
		if (numeric) return value? "1" : "0";
		return value.toString();
	}

	@Override
	public String repr(Boolean value) {
		return value.toString();
	}
}