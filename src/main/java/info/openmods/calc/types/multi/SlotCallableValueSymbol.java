package info.openmods.calc.types.multi;

import com.google.common.base.Preconditions;
import info.openmods.calc.Frame;
import info.openmods.calc.symbol.ISymbol;
import info.openmods.calc.utils.OptionalInt;

public class SlotCallableValueSymbol implements ISymbol<TypedValue> {

	private final TypedValue value;
	private final MetaObject.SlotCall slot;

	public SlotCallableValueSymbol(TypedValue value) {
		this.value = value;
		this.slot = value.getMetaObject().slotCall;
		Preconditions.checkState(this.slot != null, "Value %s it not callable", value);
	}

	@Override
	public void call(Frame<TypedValue> frame, OptionalInt argumentsCount, OptionalInt returnsCount) {
		slot.call(value, argumentsCount, returnsCount, frame);
	}

	@Override
	public TypedValue get() {
		return value;
	}

}
