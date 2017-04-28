package info.openmods.calc.types.multi;

import com.google.common.base.Preconditions;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.executable.Value;
import info.openmods.calc.parsing.ast.IParserState;
import info.openmods.calc.parsing.ast.SameStateSymbolTransition;
import info.openmods.calc.parsing.node.IExprNode;
import info.openmods.calc.parsing.node.SymbolCallNode;
import java.util.List;

public class CodeStateTransition extends SameStateSymbolTransition<IExprNode<TypedValue>> {

	private TypeDomain domain;

	public CodeStateTransition(TypeDomain domain, IParserState<IExprNode<TypedValue>> parentParserState) {
		super(parentParserState);
		this.domain = domain;
	}

	@Override
	public IExprNode<TypedValue> createRootNode(final List<IExprNode<TypedValue>> children) {
		class CodeSymbol extends SymbolCallNode<TypedValue> {

			public CodeSymbol() {
				super(TypedCalcConstants.SYMBOL_CODE, children);
			}

			@Override
			public void flatten(List<IExecutable<TypedValue>> output) {
				Preconditions.checkArgument(children.size() == 1, "'code' expects single argument");
				output.add(Value.create(Code.flattenAndWrap(domain, children.get(0))));
			}

		}

		return new CodeSymbol();
	}

}
