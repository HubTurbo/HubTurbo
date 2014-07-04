package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class SingleCheckListView extends VBox {
	
	private ObservableList<SingleCheckListItemModel> items;
	private ListView<SingleCheckListItemModel> listView;
	
	public SingleCheckListView(ObservableList<String> items) {
		
		this.listView = new ListView<SingleCheckListItemModel>();
		getChildren().add(listView);
		setItems(items);
	}
	
	public void setItems(ObservableList<String> items) {
		// It's assumed that we won't need to observe this list in the
		// long term, so we don't use the same list object
		ObservableList<SingleCheckListItemModel> newItems = FXCollections.observableArrayList();
		int x = 0;
		SingleCheckListItemModel temp = null;
		for (String s : items) {
			final int y = x;
			SingleCheckListItemModel item = new SingleCheckListItemModel(s, false);
			item.checkedProperty().addListener(new ChangeListener<Boolean>() {
		        public void changed(ObservableValue<? extends Boolean> ov,
			            Boolean oldValue, Boolean newValue) {
//			        	issue.setOpen(!newValue);
		        	System.out.println("item " + y + " changed from " + oldValue + " to " + newValue);
	        	}
		    });
			newItems.add(item);
			x++;
			temp = item;
		}
		temp.setChecked(true);
		this.items = newItems;
		refresh();
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
