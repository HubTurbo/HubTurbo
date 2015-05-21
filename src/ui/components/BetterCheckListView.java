package ui.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class BetterCheckListView extends VBox {
	
	private ObservableList<BetterCheckListItem> items;
	private ListView<BetterCheckListItem> listView;
	
	private ArrayList<ChangeListener<Boolean>> listeners;
	
	public BetterCheckListView(ObservableList<String> items) {
		
		listeners = new ArrayList<>();

		this.listView = new ListView<BetterCheckListItem>();
		getChildren().add(listView);
		setItems(items);
	}
	
	boolean singleSelection = false;
	
	public BetterCheckListView setSingleSelection(boolean single) {
		singleSelection = single;
		return this;
	}
	
	boolean disabled = false;
	
	public boolean checkItem(String itemName){
		int index = getItemIndex(itemName);
		if(index >= 0){
			setChecked(index, true);
			listView.scrollTo(index);
			return true;
		}
		return false;
	}
	
	public int getItemIndex(String itemName){
		for(int i = 0; i < items.size(); i++){
			if(items.get(i).getContents().equalsIgnoreCase(itemName)){
				return i;
			}
		}
		return -1;
	}
	
	public void setItems(ObservableList<String> items) {
		// It's assumed that we won't need to observe this list in the
		// long term, so we don't use the same list object
		
		// Old listeners can be garbage collected
		listeners.clear();
		
		ObservableList<BetterCheckListItem> newItems = FXCollections.observableArrayList();
		WeakReference<BetterCheckListView> that = new WeakReference<>(this);
		
		for (int i=0; i<items.size(); i++) {
			final int j = i;
			BetterCheckListItem item = new BetterCheckListItem(items.get(i), false);
			ChangeListener<Boolean> strongListener = new ChangeListener<Boolean>() {
		        public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
		        	if (!disabled) {
			        	if (singleSelection) {
			        		disabled = true;
			        		for (BetterCheckListItem item : that.get().items) {
			        			item.setChecked(false);
			        		}
			        		that.get().items.get(j).setChecked(newValue);
			        		disabled = false;
			        	}
		        	}
	        	}
		    };
			WeakChangeListener<Boolean> weakListener = new WeakChangeListener<Boolean>(strongListener);
			item.checkedProperty().addListener(weakListener);
			
			// Retain a reference to this listener, so it doesn't get
			// garbage collected prematurely
			listeners.add(strongListener);
			
			newItems.add(item);
		}
		this.items = newItems;
		refresh();
	}
	
	public void setChecked(int i, boolean state) {
		this.items.get(i).setChecked(state);
	}
			
	public boolean getChecked(int i) {
		return this.items.get(i).isChecked();
	}

	public List<Integer> getCheckedIndices() {
		ArrayList<Integer> result = new ArrayList<>();
		for (int i=0; i<items.size(); i++) {
			if (items.get(i).isChecked()) {
				result.add(i);
			}
		}
		return result;
	}

	private void refresh() {
		listView.setCellFactory(CheckBoxListCell.forListView(new Callback<BetterCheckListItem, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(BetterCheckListItem item) {
				return item.checkedProperty();
			}
		}));
		
		listView.setItems(items);
	}	
}
