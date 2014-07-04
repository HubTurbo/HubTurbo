package ui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.SelectionMode;

import org.controlsfx.control.CheckListView;

public class SingleCheckListView<T> extends CheckListView<T> {
	
	// State variables
	
	private int previouslyChecked = -1;
	private int previousState = 0;
	
	// Disables the callback to prevent an infinite loop (since
	// updating the check state updates the check state)
	private boolean disabled = false;
	
	// Also disables the callback. Throttles its activation
	// to once per change
	private boolean oneActivation = false;
	
	private ArrayList<ListChangeListener<?>> changeListeners = new ArrayList<ListChangeListener<?>>();

	public SingleCheckListView (ObservableList<T> objects) {
		super(objects);
		setup();
	}

	
	private ListChangeListener<T> createCheckboxSelectionChangeListener(){
		ListChangeListener<T> listener = new ListChangeListener<T>() {
			@Override
			public void onChanged(
					ListChangeListener.Change<? extends T> c) {

				while (c.next()) {
					if ((c.wasAdded() || c.wasRemoved())
							&& !disabled && !oneActivation) {
						List<Integer> currentlyChecked = getCheckModel().getSelectedIndices();
						oneActivation = true;

						if (currentlyChecked.size() == 1) {
							assert previousState != 1;
							previouslyChecked = currentlyChecked.get(0);
							previousState = 1;
						} else if (currentlyChecked.size() == 2) {
							assert previousState == 1;
							assert previouslyChecked != -1;

							// There is no state 2: it is always skipped
							previousState = 1;

							int newlyChecked = previouslyChecked == currentlyChecked
									.get(0) ? currentlyChecked.get(1)
									: currentlyChecked.get(0);

							previouslyChecked = newlyChecked;
							disabled = true;
							getCheckModel().clearAndSelect(newlyChecked);
							disabled = false;
						} else {
							assert currentlyChecked.size() == 0;
							assert previousState == 1;
							previousState = 0;
							previouslyChecked = -1;
						}
					}
				}
				oneActivation = false;
			}
		};
		changeListeners.add(listener);
		return listener;
	}
	private void setup() {
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
//		if (initialCheckedState.size() == 1) {
//			previousState = 1;
//			previouslyChecked = initialCheckedState.get(0);
//		}
		
		/**
		 * A small state machine to get around API oddities.
		 * 
		 * The goal is to implement the `multipleSelection` flag, which when
		 * false allows only a single checkbox to be checked.
		 * 
		 * Basically there are 4 states which a given pair of checkboxes can be in:
		 * 00, 01, 10, 11. This is exhaustive because we only need a second checked box
		 * to reduce the state back to 01 or 10. State 11 technically doesn't exist
		 * because we transition as soon as we enter it. Transitions: 1 bit at a time.
		 * 
		 * previousState and previouslyChecked are used to track this information.
		 * c.wasAdded() || c.wasRemoved(), disabled, and oneActivation are for throttling
		 * the execution of the callback in various ways.
		 */
		
		getCheckModel().getSelectedItems()
				.addListener(new WeakListChangeListener<T>(createCheckboxSelectionChangeListener()));
	}
	
	
}
