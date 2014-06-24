package command;

import java.util.Queue;

import model.ModelFacade;

public class Invoker {
	private ModelFacade model = new ModelFacade();
	private Queue<Command> commandQueue;
}
