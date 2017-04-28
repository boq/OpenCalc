package info.openmods.calc.parsing.ast;

import info.openmods.calc.parsing.token.Token;
import java.util.List;

public interface INodeFactory<N, O extends IOperator<O>> {

	public N createBracketNode(String openingBracket, String closingBracket, List<N> children);

	public N createOpNode(O op, List<N> children);

	public N createValueNode(Token token);

	public N createSymbolGetNode(String id);
}
