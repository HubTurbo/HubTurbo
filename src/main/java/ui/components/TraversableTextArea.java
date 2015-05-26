package ui.components;

import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.lang.ref.WeakReference;

public class TraversableTextArea extends TextArea {
	public TraversableTextArea(String text){
		this();
		setText(text);
	}
	
	public TraversableTextArea(){
		addEventFilter(KeyEvent.KEY_PRESSED, createKeyEventHandler());
	}
	
	private EventHandler<KeyEvent> createKeyEventHandler(){
		WeakReference<TraversableTextArea> selfRef = new WeakReference<>(this);
		return new EventHandler<KeyEvent>() {
	        @Override
	        public void handle(KeyEvent event) {
	            if (event.getCode() == KeyCode.TAB) {
	            	TextAreaSkin skin = (TextAreaSkin) selfRef.get().getSkin();
	                if (skin.getBehavior() instanceof TextAreaBehavior) {
	                    TextAreaBehavior behavior = (TextAreaBehavior) skin.getBehavior();
	                    if (event.isControlDown()) {
	                        behavior.callAction("InsertTab");
	                    }else if(event.isShiftDown()){
	                    	behavior.callAction("TraversePrevious");
	                    }else {
	                        behavior.callAction("TraverseNext");
	                    }
	                    event.consume();
	                }

	            }
	        }
	    };
	}
}
