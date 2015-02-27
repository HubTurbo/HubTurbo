package browserview;

public enum BrowserComponentError {
	NoSuchWindow,
	NoSuchElement,
	UnexpectedAlert,
	Unknown;
	
	public static BrowserComponentError fromErrorMessage(String errorMessage) {
		String[] lines = errorMessage.split("\\n");
		String firstLine = lines[0].trim();
		if (firstLine.startsWith("no such window") || firstLine.startsWith("chrome not reachable")) {
			return NoSuchWindow;
		} else if (firstLine.startsWith("no such element")) {
			return NoSuchElement;
		} else if(firstLine.startsWith("unexpected alert open")) {
			return UnexpectedAlert;
		} else {
			return Unknown;
		}
	}
}
