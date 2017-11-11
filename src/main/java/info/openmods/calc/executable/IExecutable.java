package info.openmods.calc.executable;

import info.openmods.calc.Frame;

@FunctionalInterface
public interface IExecutable<E> {
	public void execute(Frame<E> frame);
}
