package ui.issuepanel.comments;

import handler.IssueDetailsContentHandler;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import model.TurboComment;
import model.TurboIssue;
import ui.issuepanel.comments.IssueDetailsDisplay.DisplayType;

public class DetailsPanel extends VBox {
	protected static final int LIST_MAX_HEIGHT = 650;
	protected static final int COMMENTS_BOX_PREF_HEIGHT = 400;
	public static final int COMMENTS_CELL_HEIGHT = 200;
	public static final int COMMENTS_CELL_WIDTH = 330;
	public static final int COMMENTS_PADDING = 5;
	protected static final int DEFAULT_HEIGHT = 150;
	
	private ListView<TurboComment> listView;
	private IssueDetailsContentHandler handler;
	private TurboIssue issue;
	private DisplayType displayType;
		
	private HashMap<Long, DetailsCell> displayedCells = new HashMap<Long, DetailsCell>();
	
	private ObservableList<TurboComment> detailsList;
	
	public DetailsPanel(TurboIssue issue, IssueDetailsContentHandler handler, DisplayType displayType){
		this.issue = issue;
		this.listView = new ListView<TurboComment>();
		this.handler = handler;
		this.displayType = displayType;
		if(displayType == DisplayType.COMMENTS){
			detailsList = handler.getComments();
			
		}else{
			detailsList = handler.getIssueHistory();
		}
		setupLayout();
		loadItems();
	}
	
	protected void addListViewCellReference(Long commentId, DetailsCell cell){
		displayedCells.put(commentId, cell);
	}
	
	private void setupLayout(){
		this.setPadding(new Insets(COMMENTS_PADDING));
		this.setSpacing(COMMENTS_PADDING);
	}
	
	protected void scrollToBottom(){
		if(!listView.getItems().isEmpty()){
			listView.scrollTo(detailsList.size() - 1);
		}
	}
	
	protected void resizeListView(){
		List<Long> displayedIDs = detailsList.stream().map(item -> item.getId()).collect(Collectors.toList());
		double height;
		int size = displayedIDs.size();
		if(size == 0){
			height = 0;
		}else if(size > 10){
			height = LIST_MAX_HEIGHT;
		}else{
			Optional<Double> totalHeight = displayedIDs.stream()
					.map(id -> getHeightOfCommentCell(id))
					.reduce((accum, val) -> accum + val);
			height = Math.min(totalHeight.get(), LIST_MAX_HEIGHT);
		}
		listView.setPrefHeight(height);
	}
	
	private double getHeightOfCommentCell(long commentId){
		DetailsCell cell = displayedCells.get(commentId);
		if(cell == null){
			return DEFAULT_HEIGHT;
		}else{
			return cell.getHeight() + COMMENTS_PADDING;
		}
	}
	
	private Callback<ListView<TurboComment>, ListCell<TurboComment>> commentCellFactory(){
		WeakReference<DetailsPanel> selfRef = new WeakReference<>(this);
		Callback<ListView<TurboComment>, ListCell<TurboComment>> factory = new Callback<ListView<TurboComment>, ListCell<TurboComment>>() {
			@Override
			public ListCell<TurboComment> call(ListView<TurboComment> list) {
				return new DetailsCell(issue, displayType, handler, selfRef.get());
			}
		};
		return factory;
	}
	
	private void loadItems() {
		if(displayType == DisplayType.COMMENTS){
			loadNewCommentsBox();
		}
		setListItems();
		getChildren().add(0, listView);
		VBox.setVgrow(listView, Priority.ALWAYS);
	}

	
	private void loadNewCommentsBox(){
		CommentsEditBox box = new CommentsEditBox(handler);
		box.setPrefHeight(COMMENTS_CELL_HEIGHT);
		box.setPrefWidth(COMMENTS_CELL_WIDTH);
		
		TitledPane commentsContainer = new TitledPane("Add Comment", box);
		commentsContainer.setExpanded(false);
		commentsContainer.setPrefHeight(COMMENTS_BOX_PREF_HEIGHT);
		commentsContainer.setAlignment(Pos.BOTTOM_CENTER);
		getChildren().add(commentsContainer);
	}
	
	private void setListItems(){
		listView.setPrefWidth(COMMENTS_CELL_WIDTH);
		listView.setPrefHeight(LIST_MAX_HEIGHT);
		listView.setCellFactory(commentCellFactory());
		listView.setItems(detailsList);
	}
}
