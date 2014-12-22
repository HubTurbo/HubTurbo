package ui;

public class UIReference {

	private final static UIReference uiReferenceInstance = new UIReference();
	private UI ui;
	
	protected UIReference() {
		
	}

	public static UIReference getInstance(){
		return uiReferenceInstance;
	}
	
	public void setUI(UI ui) {
		this.ui = ui;
	}
	
	public UI getUI() {
		return ui;
	}
}
