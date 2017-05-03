package info.openmods.calc.parsing.ast;

import com.google.common.collect.Maps;
import info.openmods.calc.utils.MiscUtils;
import java.util.List;
import java.util.Map;

public abstract class MappedParserState<N> implements IParserState<N> {

	private final IAstParser<N> parser;

	private final Map<String, ISymbolCallStateTransition<N>> symbolTransitions = Maps.newHashMap();

	private final Map<String, IModifierStateTransition<N>> modifierTransitions = Maps.newHashMap();

	public MappedParserState(IAstParser<N> parser) {
		this.parser = parser;
	}

	@Override
	public IAstParser<N> getParser() {
		return parser;
	}

	@Override
	public ISymbolCallStateTransition<N> getStateForSymbolCall(String symbol) {
		final ISymbolCallStateTransition<N> stateTransition = symbolTransitions.get(symbol);
		return stateTransition != null? stateTransition : createDefaultSymbolCallStateTransition(symbol);
	}

	protected ISymbolCallStateTransition<N> createDefaultSymbolCallStateTransition(final String symbol) {
		return new ISymbolCallStateTransition<N>() {
			@Override
			public IParserState<N> getState() {
				return MappedParserState.this;
			}

			@Override
			public N createRootNode(List<N> children) {
				return createDefaultSymbolNode(symbol, children);
			}
		};
	}

	protected abstract N createDefaultSymbolNode(String symbol, List<N> children);

	@Override
	public IModifierStateTransition<N> getStateForModifier(String modifier) {
		final IModifierStateTransition<N> stateTransition = modifierTransitions.get(modifier);
		return stateTransition != null? stateTransition : createDefaultModifierStateTransition(modifier);
	}

	protected abstract IModifierStateTransition<N> createDefaultModifierStateTransition(String modifier);

	public MappedParserState<N> addStateTransition(String symbol, ISymbolCallStateTransition<N> transition) {
		MiscUtils.putOnce(symbolTransitions, symbol, transition);
		return this;
	}

	public MappedParserState<N> addStateTransition(String symbol, IModifierStateTransition<N> transition) {
		MiscUtils.putOnce(modifierTransitions, symbol, transition);
		return this;
	}
}
