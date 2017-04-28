package info.openmods.calc;

import info.openmods.calc.executable.Operator;
import info.openmods.calc.parsing.IValueParser;
import info.openmods.calc.parsing.ast.IOperatorDictionary;

public interface ICompilerMapFactory<E, M> {
	public Compilers<E, M> create(E nullValue, IValueParser<E> valueParser, IOperatorDictionary<Operator<E>> operators, Environment<E> environment);
}
