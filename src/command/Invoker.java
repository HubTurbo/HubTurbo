package command;

import java.util.Queue;

import model.Model;

public class Invoker {
	private Model model = new Model();
	private Queue<Command> commandQueue;
}
