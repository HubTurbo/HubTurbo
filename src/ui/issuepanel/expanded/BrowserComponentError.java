package ui.issuepanel.expanded;

public enum BrowserComponentError {
	NoSuchWindow,
	NoSuchElement,
	Unknown;
	
	public static BrowserComponentError fromErrorMessage(String errorMessage) {
		String[] lines = errorMessage.split("\\n");
		String firstLine = lines[0].trim();
		if (firstLine.startsWith("no such window") || firstLine.startsWith("chrome not reachable")) {
			return NoSuchWindow;
		} else if (firstLine.startsWith("no such element")) {
			return NoSuchElement;
		} else {
			return Unknown;
		}
	}
}
