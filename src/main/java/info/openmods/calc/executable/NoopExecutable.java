package info.openmods.calc.executable;

import info.openmods.calc.Frame;

public class NoopExecutable<E> implements IExecutable<E> {

	@Override
	public void execute(Frame<E> frame) {}

}