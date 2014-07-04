package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class BetterCheckListView extends VBox {
	
	private ObservableList<SingleCheckListItemModel> items;
	private ListView<SingleCheckListItemModel> listView;
	
	public BetterCheckListView(ObservableList<String> items) {
		
		this.listView = new ListView<SingleCheckListItemModel>();
		getChildren().add(listView);
		setItems(items);
	}
	
	boolean singleSelection = false;
	
	public BetterCheckListView setSingleSelection(boolean single) {
		singleSelection = single;
		return this;
	}
	
	boolean disabled = false;
	
	public void setItems(ObservableList<String> items) {
		// It's assumed that we won't need to observe this list in the
		// long term, so we don't use the same list object
		
		ObservableList<SingleCheckListItemModel> newItems = FXCollections.observableArrayList();
		WeakReference<BetterCheckListView> that = new WeakReference<>(this);
		
		for (int i=0; i<items.size(); i++) {
			final int j = i;
			SingleCheckListItemModel item = new SingleCheckListItemModel(items.get(i), false);
			item.checkedProperty().addListener(new WeakChangeListener<Boolean>(new ChangeListener<Boolean>() {
		        public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
		        	if (!disabled) {
			        	if (singleSelection) {
			        		disabled = true;
			        		for (SingleCheckListItemModel item : that.get().items) {
			        			item.setChecked(false);
			        		}
			        		that.get().items.get(j).setChecked(true);
			        		disabled = false;
			        	}
		        	}
	        	}
		    }));
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
		listView.setCellFactory(CheckBoxListCell.forListView(new Callback<SingleCheckListItemModel, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(SingleCheckListItemModel item) {
				return item.checkedProperty();
			}
		}));
		
		listView.setItems(items);
	}	
}
