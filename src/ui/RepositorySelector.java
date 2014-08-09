package ui;

import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import util.SessionConfigurations;

public class RepositorySelector extends HBox{
	final ComboBox<String> comboBox = new ComboBox<String>();
	private Consumer<String> methodOnValueChange;
	
	public RepositorySelector(){
		setupLayout();
		setupComboBox();
		getChildren().addAll(comboBox);
		comboBox.prefWidthProperty().bind(widthProperty());
	}
	
	private void setupLayout(){
		setSpacing(5);
		setPadding(new Insets(5));
		setAlignment(Pos.CENTER);
	}
	
	private void setupComboBox(){
		comboBox.setFocusTraversable(false);
		comboBox.setEditable(true);
		loadComboBoxContents();
		comboBox.valueProperty().addListener((observable, old, newVal) ->{
			if(methodOnValueChange != null){
				methodOnValueChange.accept(newVal);
			}
		});
	}
	
	public void setValue(String val){
		comboBox.setValue(val);
	}
	
	public void setComboValueChangeMethod(Consumer<String> method){
		methodOnValueChange = method;
	}
	
	private void loadComboBoxContents(){
		List<String> items = SessionConfigurations.getLastViewedRepositories();
		comboBox.getItems().addAll(items);
	}
	
	public void refreshComboBoxContents(){
		comboBox.getItems().clear();
		loadComboBoxContents();
	}
}
