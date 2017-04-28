package info.openmods.calc.types.multi;

import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.SymbolCall;
import info.openmods.calc.executable.SymbolGet;
import info.openmods.calc.executable.UnaryOperator;
import info.openmods.calc.parsing.node.IExprNode;
import info.openmods.calc.parsing.node.SymbolCallNode;
import java.util.List;

public class VarArgSymbolCallNode extends SymbolCallNode<TypedValue> {

	private final UnaryOperator<TypedValue> unpackMarker;

	public VarArgSymbolCallNode(UnaryOperator<TypedValue> unpackMarker, String symbol, List<? extends IExprNode<TypedValue>> args) {
		super(symbol, args);
		this.unpackMarker = unpackMarker;
	}

	@Override
	public void flatten(final List<IExecutable<TypedValue>> output) {
		new ArgUnpackCompilerHelper(unpackMarker) {
			@Override
			protected void compileWithVarArgs(int normalArgCount, List<IExecutable<TypedValue>> compiledArgs) {
				output.add(new SymbolGet<TypedValue>(symbol));
				output.addAll(compiledArgs);
				// normalArgCount+2 == len(target|*args|varArg list)
				output.add(new SymbolCall<TypedValue>(TypedCalcConstants.SYMBOL_APPLYVAR, normalArgCount + 2, 1));
			}

			@Override
			protected void compileWithoutVarArgs(int allArgs, List<IExecutable<TypedValue>> compiledArgs) {
				output.addAll(compiledArgs);
				output.add(new SymbolCall<TypedValue>(symbol, allArgs, 1));
			}

		}.compileArgUnpack(getChildren());
	}

}
