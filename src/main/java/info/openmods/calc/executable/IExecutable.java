package info.openmods.calc.executable;

import info.openmods.calc.Frame;

public interface IExecutable<E> {
	public void execute(Frame<E> frame);
}
