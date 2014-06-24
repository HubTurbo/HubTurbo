package command;

public interface UndoableCommand extends Command {
	public void undo();
}
